import tkinter as tk
from mfrc522 import SimpleMFRC522
import mysql.connector
from datetime import datetime
import time
import RPi.GPIO as GPIO
import threading

GPIO.setwarnings(False)

# Set up the RFID reader
reader = SimpleMFRC522()

# MySQL Database connection setup
def connect_to_db():
    try:
        conn = mysql.connector.connect(
            host="10.44.0.56",  # Your MySQL Server IP address
            user="raspi",      # Your MySQL username
            password="Raspi_123",  # Your MySQL password
            database="TK_DB"    # Your Database name
        )
        return conn
    except mysql.connector.Error as err:
        print(f"Error: {err}")
        return None

# Function to log error message into the error_logs table
def log_error_message_to_db(error_message, emp_id=None):
    try:
        # Connect to the database
        conn = connect_to_db()
        if conn is None:
            print("Failed to connect to the database.")
            return
        
        cursor = conn.cursor()

        # Insert error message into the error_logs table
        cursor.execute(""" 
            INSERT INTO error_logs (error_message, emp_id)
            VALUES (%s, %s)
        """, (error_message, emp_id))

        # Commit the changes
        conn.commit()

        # Close the cursor and connection
        cursor.close()
        conn.close()
        
        print(f"Error logged: {error_message}")

    except mysql.connector.Error as err:
        print(f"Error logging message: {err}")

def process_rfid_tag():
    try:
        print("Please scan your RFID tag...")
        id, text = reader.read()  # Read the RFID tag (id is the raw RFID number)
        print(f"Raw RFID ID (Numeric ID): {id}")  # Print the raw RFID number
        print(f"RFID Tag Content: {text.strip()}")  # Print the content (tag information)

        # Connect to the database
        conn = connect_to_db()
        if conn is None:
            print("Failed to connect to the database.")
            return
        
        cursor = conn.cursor()

        # Query the users table using the raw RFID number (id)
        cursor.execute("SELECT emp_id, name, shift_type FROM users WHERE rfid_tag = %s", (id,))
        result = cursor.fetchone()

        if result:
            emp_id = result[0]
            name = result[1]
            shift_type = result[2]  # Get the shift type ('Day' or 'Night')
            print(f"Employee ID: {emp_id}, Name: {name}, Shift Type: {shift_type}")

            # Get today's date in YYYY-MM-DD format
            today_date = datetime.now().strftime('%Y-%m-%d')

            # Query the calendar table to check if today is a holiday, or Sunday
            cursor.execute("""
                SELECT is_holiday, holiday_name, is_sunday
                FROM calendar
                WHERE date = %s
            """, (today_date,))
            calendar_data = cursor.fetchone()

            # Default values in case today is not in the calendar table
            is_holiday = False
            holiday_name = None
            is_sunday = False
            
            if calendar_data:
                is_holiday, holiday_name, is_sunday= calendar_data

            print(f"Is Holiday: {is_holiday}, Is Sunday: {is_sunday}")

            # Query the logs table to check if there is an existing time-in or time-out for today
            cursor.execute(""" 
                SELECT log_id, time_in, time_out, shift_type 
                FROM logs 
                WHERE emp_id = %s AND DATE(time_in) = %s
            """, (emp_id, today_date))
            existing_log = cursor.fetchone()

            # Get current time (for time-in)
            time_in = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
            time_in_obj = datetime.strptime(time_in, '%Y-%m-%d %H:%M:%S')

            # Check time-in restrictions based on shift type and calendar data
            if shift_type == 'Day':
                # Day shift allowed time-in: 5:00 AM to 11:00 AM
                allowed_start_time = datetime.strptime(f'{today_date} 05:00:00', '%Y-%m-%d %H:%M:%S')
                allowed_end_time = datetime.strptime(f'{today_date} 17:30:00', '%Y-%m-%d %H:%M:%S')
                
                # Adjust if it's a holiday or weekend
                if is_holiday:
                    allowed_start_time = datetime.strptime(f'{today_date} 05:00:00', '%Y-%m-%d %H:%M:%S')  # Allow later time-in for weekends/holidays
                	
                if not (allowed_start_time <= time_in_obj <= allowed_end_time):
                    show_error_message(f"Day Shift: Time-In not allowed outside your schedule for Employee ID: {emp_id}", emp_id)
                    return

            elif shift_type == 'Night':
                # Night shift allowed time-in: 5:00 PM to 11:00 PM
                allowed_start_time = datetime.strptime(f'{today_date} 17:00:00', '%Y-%m-%d %H:%M:%S')
                allowed_end_time = datetime.strptime(f'{today_date} 23:59:59', '%Y-%m-%d %H:%M:%S')

                if is_holiday:
                    allowed_end_time = datetime.strptime(f'{today_date} 23:59:59', '%Y-%m-%d %H:%M:%S')  # Extend time for weekends/holidays
                
                if not (allowed_start_time <= time_in_obj <= allowed_end_time):
                    show_error_message(f"Night Shift: Time-In not allowed outside your schedule for Employee ID: {emp_id}", emp_id)
                    return

            if existing_log:
                # If the log exists and time_out is NULL, update the time_out
                if existing_log[2] is None:
                    print("Recording Time-Out...")

                    # Get the current time for time-out
                    time_out = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
                    time_out_obj = datetime.strptime(time_out, '%Y-%m-%d %H:%M:%S')

                    # Check time-out restrictions based on shift type and calendar data
                    if shift_type == 'Day':
                        # Day shift allowed time-out: 10:30 AM to 6:00 PM
                        time_out_start = datetime.strptime(f'{today_date} 10:30:00', '%Y-%m-%d %H:%M:%S')
                        time_out_end = datetime.strptime(f'{today_date} 18:00:00', '%Y-%m-%d %H:%M:%S')
                        
                        # Adjust if it's a holiday or weekend
                        if is_holiday:
                            time_out_end = datetime.strptime(f'{today_date} 18:30:00', '%Y-%m-%d %H:%M:%S')  # Allow extended time-out for weekends/holidays

                        if not (time_out_start <= time_out_obj <= time_out_end):
                            show_error_message(f"Day Shift: Time-Out not allowed outside your schedule for Employee ID: {emp_id}", emp_id)
                            return

                    elif shift_type == 'Night':
                        # Night shift allowed time-out: 10:30 PM to 5:30 AM
                        time_out_start = datetime.strptime(f'{today_date} 22:30:00', '%Y-%m-%d %H:%M:%S')
                        time_out_end = datetime.strptime(f'{today_date} 05:30:00', '%Y-%m-%d %H:%M:%S')

                        if not (time_out_start <= time_out_obj <= time_out_end):
                            show_error_message(f"Night Shift: Time-Out not allowed outside your schedule for Employee ID: {emp_id}", emp_id)
                            return

                    # Calculate time_out_status based on the current time
                    time_out_status = calculate_time_out_status(time_out)

                    cursor.execute(""" 
                        UPDATE logs 
                        SET time_out = %s, 
                            time_out_status = %s
                        WHERE log_id = %s
                    """, (time_out, time_out_status, existing_log[0]))

                    print(f"Time-Out Recorded: {time_out}")
                    log_status = "Time-Out"
                else:
                    print("You have already completed your time-in and time-out for today.")
                    log_status = "Already completed"
            else:
                # If no existing log, record time-in
                print("Recording Time-In...")

                absent_time_day = datetime.strptime(f'{time_in[:10]} 11:00:00', '%Y-%m-%d %H:%M:%S')
                absent_time_night = datetime.strptime(f'{time_in[:10]} 23:00:00', '%Y-%m-%d %H:%M:%S')

                if shift_type == 'Day':
                    if datetime.strptime(time_in, '%Y-%m-%d %H:%M:%S') >= absent_time_day:
                        print("Employee marked as Absent (Time-In after 11:00 AM).")
                        time_in_status = 'Absent'
                        time_out = time_in  # Set Time-Out as Time-In since it's Absent
                        time_out_status = 'Absent'
                    else:
                        time_in_status = calculate_time_in_status(time_in)
                        time_out = None  # Time-out is initially NULL
                        time_out_status = None

                elif shift_type == 'Night':
                    if datetime.strptime(time_in, '%Y-%m-%d %H:%M:%S') >= absent_time_night:
                        print("Employee marked as Absent (Time-In after 11:00 PM).")
                        time_in_status = 'Absent'
                        time_out = time_in  # Set Time-Out as Time-In since it's Absent
                        time_out_status = 'Absent'
                    else:
                        time_in_status = calculate_time_in_status(time_in)
                        time_out = None  # Time-out is initially NULL
                        time_out_status = None
		
		# Determine holiday status
                if is_holiday:
    			holiday_status = holiday_name  # If it's a holiday, use the holiday_name from the calendar table
                else:
    			holiday_status = 'Regular'  # If it's not a holiday, insert 'Regular'

		# Insert the log into the database for both Day and Night shift
                cursor.execute(""" 
                    INSERT INTO logs (emp_id, rfid_tag, time_in, time_in_status, time_out, time_out_status, shift_type, holiday, sunday)
                    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
                """, (emp_id, id, time_in, time_in_status, time_out, time_out_status, shift_type, holiday_status, 'Yes' if is_sunday else 'No'))

                print(f"Time-In Recorded: {time_in}")
                log_status = "Time-In"

            # Commit changes to the database
            conn.commit()

            # Show appropriate pop-up message
            if log_status == "Time-In":
                show_success_message(f"Time-In Successful! Employee ID: {emp_id} Name: {name} Status: {time_in_status}")
            elif log_status == "Time-In" and calendar_data == is_holiday:
                show_success_message(f"Time-In Successful! Today is a holiday! Employee ID: {emp_id} Name: {name} Status: {time_in_status}")
            elif log_status == "Time-Out":
                show_success_message(f"Time-Out Successful! Employee ID: {emp_id} Name: {name} Status: {time_out_status}")
            elif log_status == "Already completed":
                show_error_message(f"You have already completed your time-in and time-out for today. Employee ID: {emp_id}")
            
        else:
            print("RFID tag not registered in users table.")
            show_error_message(f"RFID tag not registered. Please check Employee ID.")

        # Close the database connection
        cursor.close()
        conn.close()

    except Exception as e:
        print(f"Error: {e}")
        log_error_message_to_db(f"Exception occurred: {e}")

# Calculate Time-In Status for Day Shift
def calculate_time_in_status(time_in):
    time_in_obj = datetime.strptime(time_in, '%Y-%m-%d %H:%M:%S')
    
    # Define boundaries for work start, late time and absent time
    work_start_time = datetime.strptime(f'{time_in[:10]} 06:14:59', '%Y-%m-%d %H:%M:%S')
    late_start_time = datetime.strptime(f'{time_in[:10]} 06:15:00', '%Y-%m-%d %H:%M:%S')
    late_end_time = datetime.strptime(f'{time_in[:10]} 10:59:59', '%Y-%m-%d %H:%M:%S')
    absent_time = datetime.strptime(f'{time_in[:10]} 11:00:00', '%Y-%m-%d %H:%M:%S')
    
    # If Time-In is after or at 11:00 AM, consider it as Absent
    if time_in_obj >= absent_time:
        return 'Absent'
    
    # If Time-In is on or before 6:14:59 AM, it is On Time
    elif time_in_obj <= work_start_time:
        return 'On Time'
    
    # If Time-In is between 6:15 AM and 10:59:59 AM, it's considered Late
    elif late_start_time <= time_in_obj <= late_end_time:
        return 'Late'
    
    # Otherwise, return Unknown (although this case should not occur with the above conditions)
    else:
        return 'Unknown'

# Calculate Time-Out Status for Day Shift
def calculate_time_out_status(time_out):
    time_out_obj = datetime.strptime(time_out, '%Y-%m-%d %H:%M:%S')
    work_end_time = datetime.strptime(f'{time_out[:10]} 15:30:00', '%Y-%m-%d %H:%M:%S')
    normal_end_time = datetime.strptime(f'{time_out[:10]} 15:40:00', '%Y-%m-%d %H:%M:%S')
    early_out_time = datetime.strptime(f'{time_out[:10]} 10:30:00', '%Y-%m-%d %H:%M:%S')
    overtime_start_time = datetime.strptime(f'{time_out[:10]} 16:00:00', '%Y-%m-%d %H:%M:%S')
    restricted_start_time = datetime.strptime(f'{time_out[:10]} 06:30:00', '%Y-%m-%d %H:%M:%S')
    restricted_end_time = datetime.strptime(f'{time_out[:10]} 10:00:00', '%Y-%m-%d %H:%M:%S')

    if restricted_start_time <= time_out_obj <= restricted_end_time:
        return 'You have already Sucessfully Time In'
    # If the Time-Out is between 10:30 AM and 3:30 PM, it's considered Early Out
    if early_out_time <= time_out_obj <= work_end_time:
        return 'Early Out'
    # If Time-Out is between 3:30 PM and 3:40 PM, it's Normal
    elif time_out_obj <= normal_end_time:
        return 'Normal'
    # If Time-Out is after 4:00 PM but before 5:00 PM, it's Overtime
    elif overtime_start_time <= time_out_obj <= overtime_start_time.replace(hour=17, minute=0):
        return 'Overtime'
    else:
        return 'Unknown'

# Calculate Time-In Status for Night Shift
def calculate_time_in_status_night(time_in):
    time_in_obj = datetime.strptime(time_in, '%Y-%m-%d %H:%M:%S')

    # Define boundaries for normal, late, and absent time for Night Shift
    normal_start_time = datetime.strptime(f'{time_in[:10]} 17:30:00', '%Y-%m-%d %H:%M:%S')
    normal_end_time = datetime.strptime(f'{time_in[:10]} 18:14:59', '%Y-%m-%d %H:%M:%S')
    late_start_time = datetime.strptime(f'{time_in[:10]} 18:15:00', '%Y-%m-%d %H:%M:%S')
    late_end_time = datetime.strptime(f'{time_in[:10]} 22:59:59', '%Y-%m-%d %H:%M:%S')
    absent_time = datetime.strptime(f'{time_in[:10]} 23:00:00', '%Y-%m-%d %H:%M:%S')

    # If Time-In is between 5:30 PM and 6:14:59 PM, it's Normal
    if normal_start_time <= time_in_obj <= normal_end_time:
        return 'On Time'
    
    # If Time-In is between 6:15 PM and 10:59 PM, it's Late
    elif late_start_time <= time_in_obj <= late_end_time:
        return 'Late'
    
    # If Time-In is after or at 11:00 PM, consider it as Absent
    elif time_in_obj >= absent_time:
        return 'Absent'
    
    return 'Unknown'

# Calculate Time-Out Status for Night Shift
def calculate_time_out_status_night(time_out):
    time_out_obj = datetime.strptime(time_out, '%Y-%m-%d %H:%M:%S')
    work_end_time = datetime.strptime(f'{time_out[:10]} 03:30:00', '%Y-%m-%d %H:%M:%S')
    normal_end_time = datetime.strptime(f'{time_out[:10]} 03:40:00', '%Y-%m-%d %H:%M:%S')
    early_out_time = datetime.strptime(f'{time_out[:10]} 22:30:00', '%Y-%m-%d %H:%M:%S')
    overtime_start_time = datetime.strptime(f'{time_out[:10]} 04:00:00', '%Y-%m-%d %H:%M:%S')
    restricted_start_time = datetime.strptime(f'{time_out[:10]} 17:30:00', '%Y-%m-%d %H:%M:%S')
    restricted_end_time = datetime.strptime(f'{time_out[:10]} 22:29:59', '%Y-%m-%d %H:%M:%S')

    if restricted_start_time <= time_out_obj <= restricted_end_time:
        return 'You have already Succesfully Time in'
    # If the Time-Out is between 10:30 AM and 3:30 PM, it's considered Early Out
    if early_out_time <= time_out_obj <= work_end_time:
        return 'Early Out'
    # If Time-Out is between 3:30 PM and 3:40 PM, it's Normal
    elif time_out_obj <= normal_end_time:
        return 'Normal'
    # If Time-Out is after 4:00 PM but before 5:00 PM, it's Overtime
    elif overtime_start_time <= time_out_obj <= overtime_start_time.replace(hour=4, minute=0):
        return 'Overtime'
    else:
        return 'Unknown'

# Function to show a custom success message (with auto-close)
def show_success_message(message):
    root.after(0, show_message_window, "Success", "#4CAF50", message)

# Function to show an error message (if RFID tag is not registered)
def show_error_message(message, emp_id=None):
    log_error_message_to_db(message, emp_id)
    root.after(0, show_message_window, "Error", "#F44336", message)

# Function to display a message window (success or error)
def show_message_window(title, bg_color, message):
    msg_window = tk.Toplevel(root)
    msg_window.title(title)
    msg_window.geometry("400x200")  # Set the window size
    msg_window.configure(bg=bg_color)

    label = tk.Label(msg_window, text=message, font=("Helvetica", 14), fg="white", bg=bg_color, width=40, height=2, wraplength=400)
    label.pack(expand=True)

    # Center the window on the screen
    window_width = 400
    window_height = 200
    screen_width = root.winfo_screenwidth()
    screen_height = root.winfo_screenheight()
    position_top = int(screen_height / 2 - window_height / 2)
    position_right = int(screen_width / 2 - window_width / 2)
    msg_window.geometry(f'{window_width}x{window_height}+{position_right}+{position_top}')

    # Close the message box after 2 seconds
    root.after(5000, msg_window.destroy)

# Function to continuously check and update the holiday status
def update_holiday_status():
    try:
        # Get today's date in YYYY-MM-DD format
        today_date = datetime.now().strftime('%Y-%m-%d')
        print(f"Today's date for query: {today_date}")

        # Connect to the database
        conn = connect_to_db()
        if conn is None:
            print("Failed to connect to the database.")
            return

        cursor = conn.cursor()

        # Query the calendar table to check if today is a holiday or Sunday
        cursor.execute("""
            SELECT is_holiday, is_sunday, holiday_name
            FROM calendar
            WHERE date = %s
        """, (today_date,))
        calendar_data = cursor.fetchone()

        # Default values in case today is not in the calendar table
        is_holiday = False
        is_sunday = False
        holiday_name = ""

        if calendar_data:
            is_holiday, is_sunday, holiday_name = calendar_data
            print(f"Calendar data fetched: is_holiday={is_holiday}, is_sunday={is_sunday}, holiday_name={holiday_name}")
        else:
            print(f"No calendar data found for {today_date}")

        # Update the holiday label text based on the data
        holiday_label_text = ""
        if is_holiday:
            holiday_label_text = f"Holiday: {holiday_name}"
        elif is_sunday:
            holiday_label_text = "Today is Sunday!"

        print(f"Holiday Label Text: {holiday_label_text}")

        # Use root.after to update the holiday label safely on the main thread
        root.after(0, update_holiday_label, holiday_label_text)

        # Close the database connection
        cursor.close()
        conn.close()

    except Exception as e:
        print(f"Error: {e}")
        log_error_message_to_db(f"Exception occurred: {e}")

    # Schedule the next check in 60 seconds
    root.after(60000, update_holiday_status)  # Update every minute

# Function to update the holiday label in a thread-safe way
def update_holiday_label(holiday_label_text):
    holiday_label.config(text=holiday_label_text)

# Function to update the time in the top-right corner
def update_time():
    global current_time, current_date
    current_time = datetime.now().strftime("%H:%M:%S")
    current_date = datetime.now().strftime("%Y-%m-%d")
    time_label.config(text=f"TIME: {current_time}")
    date_label.config(text=f"DATE: {current_date}")
    root.after(1000, update_time)  # Update every second

# Function to show the idle "Tap your ID on the RFID reader" message
def show_idle_message():
    global idle_message_label
    if idle_message_label:
        idle_message_label.destroy()  # Destroy previous idle message if exists
    idle_message_label = tk.Label(root, text="Tap your ID on the RFID reader", font=("Helvetica", 20), fg="black", bg="PowderBlue", width=40, height=4, wraplength=750)
    idle_message_label.place(relx=0.5, rely=0.5, anchor="center")  # Center the label

def scan_rfid():
    while True:
        try:
            # The scan process happens in this loop
            process_rfid_tag()  # Make sure process_rfid_tag does not interact with GUI directly, use root.after instead.
        except Exception as e:
            log_error_message_to_db(f"Error during RFID scan: {e}")
        time.sleep(1)  # Sleep for a second before scanning again

# Set up the Tkinter GUI
def start_gui():
    global root, idle_message_label, time_label, date_label, holiday_label, current_time, current_date
    idle_message_label = None  # Initialize idle_message_label to avoid errors
    root = tk.Tk()
    root.title("Timekeeping System")
    
    # Set window size for full screen
    root.geometry("800x480")
    root.configure(bg="#f0f0f0")

    # Create labels for displaying time and date in the top-right corner
    time_label = tk.Label(root, font=("Helvetica", 14), fg="black", bg="#f0f0f0", anchor="ne")
    time_label.place(relx=0.98, rely=0.02, anchor="ne")  # Top-right corner

    date_label = tk.Label(root, font=("Helvetica", 12), fg="black", bg="#f0f0f0", anchor="ne")
    date_label.place(relx=0.98, rely=0.06, anchor="ne")  # Just below the time

    # Create the label for displaying holiday information
    holiday_label = tk.Label(root, font=("Helvetica", 12), fg="black", bg="#f0f0f0", anchor="ne")
    holiday_label.place(relx=0.98, rely=0.10, anchor="ne")  # Below the date label

    # Initially show the "Tap your ID on the RFID reader" message
    show_idle_message()

    # Start the time update in a separate thread
    threading.Thread(target=update_time, daemon=True).start()
# Start the holiday status update in a separate thread
    threading.Thread(target=update_holiday_status, daemon=True).start()
    threading.Thread(target=scan_rfid, daemon=True).start()

    # Run the GUI loop
    root.mainloop()

# Run the GUI
start_gui()

