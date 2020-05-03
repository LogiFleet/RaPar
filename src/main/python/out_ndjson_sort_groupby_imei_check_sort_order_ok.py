
import pandas as pd
import sys
from termcolor import cprint

df = pd.read_json (r'../../../data/out-ndjson-sort-groupby-imei.txt', lines=True)

# df['line_diff'] = (df.groupby(['imei'])['line'].diff().fillna('na'))
df['line_diff'] = (df.groupby(['imei'])['line'].diff())

print(df.info())

print(df[['imei', 'line', 'line_diff']])
print("\n")

print("Write output file.. " + str(df.index) + " line(s)\n")
df.to_json("../../../data/out-ndjson-sort-groupby-imei-line-diff.txt", orient="records", lines=True)

print("Check for negative line diff result (= sorted order sequence issue in file line #):\n")

df_bad_line_order_index = df.index[df['line_diff'] < 0]

size = df_bad_line_order_index.size
index = df_bad_line_order_index

print ("size: " + str(size))
print ("index: " + str(index) + "\n")

if size != 0:
    print("Col = index line line_diff imei")
    df_bad_line_order = df[df['line_diff'] < 0]
    for index, row in df_bad_line_order.iterrows():
        # access data using column names
        print(index, row['line'], row['line_diff'], row['imei'])

    print("\n")

if size != 0:
    out = "Attention! Result is NOT OK for: " + str(size) + " match"
    cprint(out, 'red', attrs=['bold'], file=sys.stderr)
else:
    cprint("Result is OK!", 'green', attrs=['bold'], file=sys.stderr)

