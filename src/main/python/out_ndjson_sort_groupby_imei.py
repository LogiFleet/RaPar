
import pandas as pd

df = pd.read_json (r'../../../data/out-ndjson.txt', lines=True)

print(df.info())

df.sort_values(by='imei', inplace=True, kind='mergesort')

df.to_json("../../../data/out-ndjson-sort-groupby-imei.txt", orient="records", lines=True)

