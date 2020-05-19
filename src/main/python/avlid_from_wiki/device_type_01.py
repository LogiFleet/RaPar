
##############################################################
#
#
# !!!!!! Be aware to comment/uncomment with or without description line below in code,
# either to fill ...AVL-ID.txt or ...AVL-ID-DESCRIPTION.txt file
#
#
##############################################################

import traceback
import requests
import pandas as pd
import fileinput
import os
import csv

# Supported Teltonika devices AVL ID wiki page:
# -FM3001
# -FMM130
# -FMC130

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

# Teltonika FM3001
url = 'https://wiki.teltonika-gps.com/view/FM3001_AVL_ID'

# Teltonika FM36M1 (warning, not the same page structure as the others)
# url = 'https://wiki.teltonika-gps.com/view/FM36M1_AVL_ID'

# Teltonika FMM130
# url = 'https://wiki.teltonika-gps.com/view/FMM130_AVL_ID'

# Teltonika FMC130
# url = 'https://wiki.teltonika-gps.com/view/FMC130_AVL_ID'

html = requests.get(url).content
df_list = pd.read_html(html)

# # debug
# print(len(df_list))
#
# df = df_list[0]
#
# print(df.info())
#
# df[['Property ID in AVL packet', 'Property Name']].to_csv(file_name_3, mode='a', sep=':', index=False, header=False, encoding='utf-8')

# prod
# BEGIN specific to FM3001, FMM130, FMC130
for i in range(len(df_list)):
    df = df_list[i]

    # Without description
    # df[['Property ID in AVL packet', 'Property Name']].to_csv(file_name_1, mode='a', sep=':', index=False, header=False, encoding='utf-8')

    # With description
    df[['Property ID in AVL packet', 'Property Name', 'Description']].to_csv(file_name_1, mode='a', sep=':', index=False, header=False, encoding='utf-8')

# END specific to FM3001, FMM130, FMC130

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

