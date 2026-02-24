import urllib.request
import json
import uuid
import os

DIVISIONS_URL = "https://raw.githubusercontent.com/nuhil/bangladesh-geocode/master/divisions/divisions.json"
DISTRICTS_URL = "https://raw.githubusercontent.com/nuhil/bangladesh-geocode/master/districts/districts.json"
UPAZILAS_URL = "https://raw.githubusercontent.com/nuhil/bangladesh-geocode/master/upazilas/upazilas.json"

def fetch_json(url):
    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
    with urllib.request.urlopen(req) as response:
        return json.loads(response.read().decode('utf-8'))

divisions = fetch_json(DIVISIONS_URL)
districts = fetch_json(DISTRICTS_URL)
upazilas = fetch_json(UPAZILAS_URL)

div_map = {} # div_id (string/int) -> uuid
dist_map = {} # dist_id (string/int) -> uuid

sql_lines = []
sql_lines.append("-- ==========================================")
sql_lines.append("-- Seed Bangladesh Locations (Divisions, Zillas, Upazilas)")
sql_lines.append("-- ==========================================")
sql_lines.append("")

sql_lines.append("-- Divisions")
for div in divisions[2]['data']:
    uid = str(uuid.uuid4())
    div_map[div['id']] = uid
    name_en = div['name'].replace("'", "''")
    name_bn = div['bn_name'].replace("'", "''")
    sql_lines.append(f"INSERT INTO locations (id, name_en, name_bn, parent_id) VALUES ('{uid}', '{name_en}', '{name_bn}', NULL);")

sql_lines.append("")
sql_lines.append("-- Districts (Zillas)")
for dist in districts[2]['data']:
    uid = str(uuid.uuid4())
    dist_map[dist['id']] = uid
    parent_id = div_map[dist['division_id']]
    name_en = dist['name'].replace("'", "''")
    name_bn = dist['bn_name'].replace("'", "''")
    sql_lines.append(f"INSERT INTO locations (id, name_en, name_bn, parent_id) VALUES ('{uid}', '{name_en}', '{name_bn}', '{parent_id}');")

sql_lines.append("")
sql_lines.append("-- Upazilas")
for upz in upazilas[2]['data']:
    uid = str(uuid.uuid4())
    parent_id = dist_map[upz['district_id']]
    name_en = upz['name'].replace("'", "''")
    name_bn = upz['bn_name'].replace("'", "''")
    sql_lines.append(f"INSERT INTO locations (id, name_en, name_bn, parent_id) VALUES ('{uid}', '{name_en}', '{name_bn}', '{parent_id}');")

# Write to V2__seed_locations.sql
out_path = r"f:\InnovativeNovaTech\ShareInt\backend\src\main\resources\db\migration\V2__seed_locations.sql"
with open(out_path, "w", encoding="utf-8") as f:
    f.write("\n".join(sql_lines))

print(f"Successfully generated {out_path} with {len(divisions[2]['data'])} divisions, {len(districts[2]['data'])} districts, and {len(upazilas[2]['data'])} upazilas.")
