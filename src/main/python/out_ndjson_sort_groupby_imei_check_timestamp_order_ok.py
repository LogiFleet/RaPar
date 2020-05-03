
import pandas as pd
import sys
from termcolor import cprint

df = pd.read_json (r'../../../data/out-ndjson-sort-groupby-imei-line-diff.txt', lines=True)

df['timeStamp_diff'] = (df.groupby(['imei'])['timeStamp'].diff())

print(df.info())

print(df[['imei', 'timeStamp', 'timeStamp_diff']])
print("\n")

print("Write output file.. " + str(df.index) + " line(s)\n")
df.to_json("../../../data/out-ndjson-sort-groupby-imei-timeStamp-diff.txt", orient="records", lines=True)

print("Check for negative timeStamp diff result (= sorted order sequence issue in file timeStamp by line #):\n")

df_bad_timeStamp_order_index = df.index[df['timeStamp_diff'] < pd.Timedelta(0)]

size = df_bad_timeStamp_order_index.size
index = df_bad_timeStamp_order_index

print ("size: " + str(size))
print ("index: " + str(index) + "\n")

if size != 0:
    print("Col = index line imei timeStamp timeStamp_diff[s]")
    df_bad_timeStamp_order = df[df['timeStamp_diff'] < pd.Timedelta(0)]
    for index, row in df_bad_timeStamp_order.iterrows():
        # access data using column names
        print(index, row['line'], row['imei'], row['timeStamp'], row['timeStamp_diff'].total_seconds())

    print("\n")

if size != 0:
    out = "Attention! Result is NOT OK for: " + str(size) + " match"
    cprint(out, 'red', attrs=['bold'], file=sys.stderr)
else:
    cprint("Result is OK!", 'green', attrs=['bold'], file=sys.stderr)

