import csv
import fileinput
import os
import traceback

import pandas as pd
import requests

#todo AVL-ID + DESCRIPTION

# Supported Teltonika devices AVL ID wiki page:
# -FM36M1

file_name_1 = "file1.txt"
file_name_2 = "file2.txt"
file_name_3 = "avlid.txt"

# start by deleting "old" out file
try:
    os.remove(file_name_3)
except Exception as e:
    print("File not accessible: " + file_name_3)
    track = traceback.format_exc()
    print(track)

# Teltonika FM36M1 (warning, not the same page structure as the others)
url = 'https://wiki.teltonika-gps.com/view/FM36M1_AVL_ID'

html = requests.get(url).content
df_list = pd.read_html(html)

# debug
# print(len(df_list))
#
# df = df_list[0]
#
# df= df.dropna()
#
# df.columns = ['c0', 'c1', 'c2', 'c3', 'c4', 'c5', 'c6', 'c7', 'c8', 'c9', 'c10', 'c11']
#
# # using dictionary to convert specific columns
# convert_dict = {'c1': int}
#
# df = df.astype(convert_dict)
#
# print(df.info())
#
# df[['c1', 'c2']].to_csv(file_name_1, mode='a', sep=':', index=False, header=False, encoding='utf-8')
#
# # df[['Property ID in AVL packet', 'Property Name']].to_csv(file_name_1, mode='a', sep=':', index=False, header=False, encoding='utf-8')

# prod
# BEGIN specific to FM36M1
convert_dict = {'c1': int}
df = df_list[0]
df = df.dropna()
df.columns = ['c0', 'c1', 'c2', 'c3', 'c4', 'c5', 'c6', 'c7', 'c8', 'c9', 'c10', 'c11']
df = df.astype(convert_dict)
df[['c1', 'c2']].to_csv(file_name_1, mode='a', sep=':', index=False, header=False, encoding='utf-8')
# END specific to FM36M1

# todo Factorize common part below
# BEGIN common
# remove duplicate line
lines_seen = set() # holds lines already seen
outfile = open(file_name_2, "w")
for line in open(file_name_1, "r"):
    if line not in lines_seen: # not a duplicate
        outfile.write(line)
        lines_seen.add(line)
outfile.close()

# replace white space by underscore
with fileinput.FileInput(file_name_2, inplace=True) as file:
    for line in file:
        print(line.replace(' ', '_'), end='')
    file.close()

# sort
f = open(file_name_2)
reader = csv.reader(f, delimiter=":")
outfile = open(file_name_3, "w", newline='')
writer = csv.writer(outfile, delimiter =':')
for line in sorted(reader, key=lambda x : int(x[0])):
    writer.writerow(line)
outfile.close()
f.close()

# clean tmp file
try:
    os.remove(file_name_1)
except Exception as e:
    print("File not accessible: " + file_name_1)
    track = traceback.format_exc()
    print(track)

try:
    os.remove(file_name_2)
except Exception as e:
    print("File not accessible: " + file_name_2)
    track = traceback.format_exc()
    print(track)
# END common

#...

