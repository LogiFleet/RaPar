
import os
import pandas as pd
import openpyxl


# For small

# pd.read_json("../../../data/out-ndjson.txt", lines=True).to_excel("../../../data/out-ndjson.xlsx")
#
# os.system('start "excel" "../../../data/out-ndjson.xlsx"')

df = pd.read_json("../../../data/out-ndjson.txt", lines=True)

# Maximum width of an excel cell = 255 characters, which is not enough to contain the raw data
# df.drop("raw", axis=1, inplace=True)

# print (df.info())

df.to_excel("../../../data/out-ndjson.xlsx")

os.system('start "excel" "../../../data/out-ndjson.xlsx"')

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
