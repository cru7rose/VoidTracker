import json

log_file = 'logs/order-service.log'

try:
    with open(log_file, 'r') as f:
        lines = f.readlines()
        
    for line in reversed(lines):
        try:
            entry = json.loads(line)
            if entry.get('level') == 'ERROR' or 'Exception' in entry.get('log_message', ''):
                print("Found Error Log:")
                print(entry.get('log_message'))
                break
        except json.JSONDecodeError:
            continue
            
except FileNotFoundError:
    print(f"File {log_file} not found")
