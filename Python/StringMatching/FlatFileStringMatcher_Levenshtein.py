from GraphToolbox import GraphManager
from InvertedIndexToolbox import getNGrams
from InvertedIndexToolbox import InvertedIndex
import operator
import pprint
import os
import shutil
import pandas as pd
import numpy as np

THRESHOLD = 0.4

def sortkey(val):
    return val[1]

def aggregate_to_dict(indices):
    tmp_tgt_ind = dict()
    for index in indices:
            if index in tmp_tgt_ind.keys():
                tmp_tgt_ind[index] = tmp_tgt_ind[index] + 1
            else:
                tmp_tgt_ind[index] = 1
    return tmp_tgt_ind


def batch_match(csv_file1, csv_file2, id_column1, id_column2, name_column1, name_column2, csv_outputfile):
    print("Reading data...")
    df1 = pd.read_csv(csv_file1, sep=',', error_bad_lines=False, encoding = "ISO-8859-1")
    df2 = pd.read_csv(csv_file2, sep=',', encoding = "ISO-8859-1")
    df1.columns = ['x_'+col for col in df1.columns]
    df2.columns = ['y_'+col for col in df2.columns]
    name_column1 = "x_" + name_column1
    name_column2 = "y_" + name_column2
    id_column1 = "x_" + id_column1
    id_column2 = "y_" + id_column2
    df1['name_length'] = df1[name_column1].str.len()
    df2['name_length'] = df2[name_column2].str.len()

    iindex1 = InvertedIndex()
    for index, row in df1.iterrows():
        iindex1.addToIndex(row[name_column1], index)

    matches = set()
    long_matches = None
    print("Matching data...")
    i = 0
    for index, row in df2.iterrows():
        i=i+1
        if i % 1000 == 0:
            progress = i * 100 / (df2.size/len(df2.columns))
            print("Done " + str(progress) + "%")
        indices = iindex1.getIndicesForValue(row[name_column2])
        tmp_tgt_ind = aggregate_to_dict(indices)
        best_matching_resources = list(tmp_tgt_ind.items())
        if best_matching_resources is None or best_matching_resources == []:
            continue
        best_matching_resources.sort(key = sortkey, reverse = True)
        best_matching_resources = np.array(best_matching_resources)
        #print(best_matching_resources)

        tmp = df1.loc[best_matching_resources[:,0]]
        tmp['ngrammatches'] = best_matching_resources[:,1]
        tmp['join'] = 0
        rid = row[id_column2]
        rname = row[name_column2]
        row = row.to_frame().transpose()
        row['join'] = 0
        rows = tmp.merge(row, on='join')
        rows['minlen'] = pd.np.minimum(rows['name_length_x'], rows['name_length_y'])
        rows['minlen'] = (rows['minlen']-2)*(THRESHOLD*0.5)
        rows = rows.loc[rows['minlen'] <= rows['ngrammatches']]
        if len(rows)>0:
            for i2,r2 in rows.iterrows():
                if not csv_file1 == csv_file2 and not r2[id_column1] == rid:
                    name1=r2[name_column1]
                    name2=rname
                    lev_dist = 1.0-iterative_levenshtein(r2[name_column1], rname)/max(len(str(r2[name_column1])), len(str(rname)))
                    if lev_dist > THRESHOLD:
                        matches.add(r2[id_column1])
                        if long_matches is None:
                            long_matches = r2.to_frame().transpose()
                        else:
                            long_matches = pd.concat([long_matches, r2.to_frame().transpose()])
    long_matches = long_matches[[id_column1, name_column1, id_column2, name_column2]]
    long_matches.columns = [col.replace('x_','').replace('y_','') for col in long_matches.columns]
    long_matches.to_csv(path_or_buf=csv_outputfile,sep=",")



def iterative_levenshtein(s, t):
        s = str(s)
        t = str(t)
        if len(s) == 0 or len(t) == 0:
            return 999999

        """
            iterative_levenshtein(s, t) -> ldist
            ldist is the Levenshtein distance between the strings
            s and t.
            For all i and j, dist[i,j] will contain the Levenshtein
            distance between the first i characters of s and the
            first j characters of t
        """
        rows = len(s)+1
        cols = len(t)+1
        dist = [[0 for x in range(cols)] for x in range(rows)]
        # source prefixes can be transformed into empty strings
        # by deletions:
        for i in range(1, rows):
            dist[i][0] = i
        # target prefixes can be created from an empty source string
        # by inserting the characters
        for i in range(1, cols):
            dist[0][i] = i

        for col in range(1, cols):
            for row in range(1, rows):
                if s[row-1] == t[col-1]:
                    cost = 0
                else:
                    cost = 1
                dist[row][col] = min(dist[row-1][col] + 1,      # deletion
                                     dist[row][col-1] + 1,      # insertion
                                     dist[row-1][col-1] + cost) # substitution



        return dist[row][col]

if __name__ == '__main__':
    csv_file1 = "C:/Users/D072202/RData2Graph/rdata2graph/data/sap_hilti_data/MARA_FERT_flattened"
    csv_file2 = "C:/Users/D072202/RData2Graph/rdata2graph/data/sap_hilti_data/Products_flattened"
    name_column1 = "MARA_FERT.MAKTX;;"
    name_column2 = 'Products.name;;'
    id_column1 = "MARA_FERT.id;id;x"
    id_column2 = "Products.id;id;x"
    csv_outputfile = 'C:/Users/D072202/RData2Graph/rdata2graph/data/sap_hilti_data/LevSyntactic_matches'
    batch_match(csv_file1, csv_file2, id_column1, id_column2, name_column1, name_column2, csv_outputfile)

