
import os

import pandas

pandas.read_json("../../../data/out-ndjson.txt", lines=True).to_excel("../../../data/out-ndjson.xlsx")

os.system('start "excel" "../../../data/out-ndjson.xlsx"')

