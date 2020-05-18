
# split out by max 50 k lines which is the max for kibana drop file feature
# to elasticsearch ndjson ingest

lines_per_file = 49999
smallfile = None
with open('../../../data/out-ndjson.txt') as bigfile:
    for lineno, line in enumerate(bigfile):
        if lineno % lines_per_file == 0:
            if smallfile:
                smallfile.close()
            small_filename = '../../../data/out-ndjson_{}.txt'.format(lineno + lines_per_file)
            smallfile = open(small_filename, "w")
        smallfile.write(line)
    if smallfile:
        smallfile.close()



