

import os
import pandas as pd
from pandas.io import json
from p_tqdm import t_map    #SRC: https://pypi.org/project/p-tqdm/
import openpyxl


# For small json file

# pd.read_json("../../../data/out-ndjson.txt", lines=True).to_excel("../../../data/out-ndjson.xlsx")
#
# os.system('start "excel" "../../../data/out-ndjson.xlsx"')

# For medium json file

# df = pd.read_json("../../../data/out-ndjson.txt", lines=True)

# For large json file

print('1. Open file and map to json..')

with open('../../../data/out-ndjson.txt') as json_file:
    data = json_file.readlines()

    # this line below may take at least 8-10 minutes of processing for 4-5 million rows. It converts all strings in list to actual json objects.
    data = list(t_map(json.loads, data))

print('2. Convert data to pandas data frame..')

df = pd.DataFrame(data)

###

# Maximum width of an excel cell = 255 characters, which is not enough to contain the raw data
# df.drop("raw", axis=1, inplace=True)

# print (df.info())

print('3. Data frame to excel.. (may take a few minutes to complete, eg 5 min/200k lines)')

df.to_excel("../../../data/out-ndjson.xlsx")

print('4. Launch excel file..')

os.system('start "excel" "../../../data/out-ndjson.xlsx"')

print('5. End.')

# For huge npm ndjson to csv then import "manually" in excel:
# cd /mnt/c/git/RaPar/data
# ndjson-to-csv out-ndjson.txt > out-ndjson.csv

###################################################################################################

#
#
# # path = '../../../data/test.xlsm'
# #
# # with pd.ExcelWriter(path) as writer:
# #     writer.book = openpyxl.load_workbook(path)
# #     df.to_excel(writer, sheet_name='feuil1')
# #
# # os.system('start "excel" "../../../data/test.xlsm"')
#
#
# writer = pd.ExcelWriter('test.xlsx', engine='xlsxwriter')
#
# df.to_excel(writer, sheet_name='Sheet1')
#
# workbook  = writer.book
# workbook.filename = 'test.xlsm'
# workbook.add_vba_project('./vbaProject.bin')
#
# writer.save()
