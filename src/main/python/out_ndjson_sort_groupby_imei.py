
import pandas as pd

df = pd.read_json (r'../../../data/out-ndjson.txt', lines=True)

print(df.info())

print("\n")

df.sort_values(by='imei', inplace=True, kind='mergesort')

print("Write output file.. " + str(df.index) + " line(s)\n")

#todo fix df.to_json MemoryError for large file

df.to_json("../../../data/out-ndjson-sort-groupby-imei.txt", orient="records", lines=True)

