import uuid
import random
from datetime import datetime, timedelta

# Configuration
NUM_ORDERS = 5000
CUSTOMERS = [
    {"name": "Acme Warsaw", "type": "B2B", "email": "contact@acme.pl", "city": "Warszawa", "zip": "00-001", "street": "Aleje Jerozolimskie", "lat": 52.2297, "lon": 21.0122},
    {"name": "Krakow Logistics", "type": "B2B", "email": "info@kraklog.pl", "city": "Kraków", "zip": "30-001", "street": "Rynek Główny", "lat": 50.0647, "lon": 19.9450},
    {"name": "Gdansk Imports", "type": "B2B", "email": "sea@gdanskimports.pl", "city": "Gdańsk", "zip": "80-001", "street": "Długa", "lat": 54.3520, "lon": 18.6466},
    {"name": "Wroclaw Tech", "type": "B2B", "email": "ops@wroclawtech.pl", "city": "Wrocław", "zip": "50-001", "street": "Świdnicka", "lat": 51.1079, "lon": 17.0385},
    {"name": "Poznan Distribution", "type": "B2B", "email": "dist@poznan.pl", "city": "Poznań", "zip": "60-001", "street": "Święty Marcin", "lat": 52.4064, "lon": 16.9252},
    {"name": "Lodz Textiles", "type": "B2B", "email": "fabrics@lodz.pl", "city": "Łódź", "zip": "90-001", "street": "Piotrkowska", "lat": 51.7592, "lon": 19.4560},
    {"name": "Szczecin Maritime", "type": "B2B", "email": "port@szczecin.pl", "city": "Szczecin", "zip": "70-001", "street": "Wały Chrobrego", "lat": 53.4285, "lon": 14.5528},
]

POLISH_CITIES = [
    {"city": "Warszawa", "zip": "01-001", "lat_base": 52.2, "lon_base": 21.0},
    {"city": "Kraków", "zip": "31-001", "lat_base": 50.0, "lon_base": 19.9},
    {"city": "Gdańsk", "zip": "80-100", "lat_base": 54.3, "lon_base": 18.6},
    {"city": "Wrocław", "zip": "50-100", "lat_base": 51.1, "lon_base": 17.0},
    {"city": "Poznań", "zip": "61-001", "lat_base": 52.4, "lon_base": 16.9},
    {"city": "Bydgoszcz", "zip": "85-001", "lat_base": 53.1, "lon_base": 18.0},
    {"city": "Lublin", "zip": "20-001", "lat_base": 51.2, "lon_base": 22.5},
    {"city": "Katowice", "zip": "40-001", "lat_base": 50.2, "lon_base": 19.0},
]

STREET_NAMES = ["Polna", "Leśna", "Słoneczna", "Krótka", "Szkolna", "Ogrodowa", "Lipowa", "Brzozowa", "Łąkowa", "Kwiatowa"]

def escape_sql(val):
    if val is None: return "NULL"
    return "'" + str(val).replace("'", "''") + "'"

def generate_random_point(lat, lon, radius=0.1):
    return lat + random.uniform(-radius, radius), lon + random.uniform(-radius, radius)

sql_lines = []
sql_lines.append("BEGIN;")
sql_lines.append("TRUNCATE TABLE vt_orders CASCADE;")
sql_lines.append("TRUNCATE TABLE vt_addresses CASCADE;")
sql_lines.append("TRUNCATE TABLE vt_customers CASCADE;")

customer_ids = []

# 1. Generate Customers
for c in CUSTOMERS:
    cust_id = str(uuid.uuid4())
    customer_ids.append(cust_id)
    sql_lines.append(f"INSERT INTO vt_customers (customer_id, customer_name, customer_type, contact_email) VALUES ('{cust_id}', '{c['name']}', '{c['type']}', '{c['email']}');")
    
    # Generate Customer Address (Sender)
    addr_id = str(uuid.uuid4())
    sql_lines.append(f"INSERT INTO vt_addresses (address_id, owner_customer_id, city, street, postal_code, lat, lon) VALUES ('{addr_id}', '{cust_id}', '{c['city']}', '{c['street']}', '{c['zip']}', {c['lat']}, {c['lon']});")
    c['address_id'] = addr_id # Store for reuse as sender

# 2. Generate Orders
tomorrow = datetime.now() + timedelta(days=1)
start_time = tomorrow.replace(hour=8, minute=0, second=0)

for i in range(NUM_ORDERS):
    cust_idx = random.randint(0, len(CUSTOMERS) - 1)
    customer = CUSTOMERS[cust_idx]
    cust_id = customer_ids[cust_idx]
    
    # Pickup is always from Customer warehouse for simplicity in this B2B scenario, or simulated diverse pickup.
    # User said "aliases for the addresses". Let's assume unique pickups per customer.
    pickup_addr_id = customer['address_id']
    
    # Generate Delivery Address
    target_city = random.choice(POLISH_CITIES)
    del_lat, del_lon = generate_random_point(target_city['lat_base'], target_city['lon_base'])
    del_addr_id = str(uuid.uuid4())
    
    street = random.choice(STREET_NAMES)
    number = random.randint(1, 150)
    
    sql_lines.append(f"INSERT INTO vt_addresses (address_id, owner_customer_id, city, street, street_number, postal_code, lat, lon) VALUES ('{del_addr_id}', '{cust_id}', '{target_city['city']}', '{street}', '{number}', '{target_city['zip']}', {del_lat}, {del_lon});")
    
    # Order
    order_id = str(uuid.uuid4())
    status = "NEW" # Corrected from PLANNED
    
    # Time windows
    delivery_from = start_time + timedelta(minutes=random.randint(0, 480)) # 8:00 - 16:00
    delivery_to = delivery_from + timedelta(hours=2)
    
    sql_lines.append(f"INSERT INTO vt_orders (order_id, ordering_customer_id, pickup_address_id, delivery_address_id, status, created, delivery_time_from, delivery_time_to, delivery_type, priority) VALUES ('{order_id}', '{cust_id}', '{pickup_addr_id}', '{del_addr_id}', '{status}', NOW(), '{delivery_from}', '{delivery_to}', 'DAY_DELIVERY', 'NORMAL');")

sql_lines.append("COMMIT;")

with open("scripts/seed_data.sql", "w") as f:
    f.write("\n".join(sql_lines))

print(f"Generated seed_data.sql with {NUM_ORDERS} orders.")
