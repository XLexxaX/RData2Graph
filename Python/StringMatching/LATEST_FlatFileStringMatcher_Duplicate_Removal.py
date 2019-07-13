from StringMatching.GraphToolbox import GraphManager
from StringMatching.InvertedIndexToolbox import getNGrams
from StringMatching.InvertedIndexToolbox import InvertedIndex
import operator
import pprint
import os
import shutil
import pandas as pd
import numpy as np

THRESHOLD = 0.99

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
        row = row.to_frame().transpose()
        row['join'] = 0
        rows = tmp.merge(row, on='join')
        rows['minlen'] = pd.np.minimum(rows['name_length_x'], rows['name_length_y'])
        rows['minlen'] = (rows['minlen']-2)*THRESHOLD
        rows = rows.loc[rows['minlen'] <= rows['ngrammatches']]
        if len(rows)>0:
            for i2,r2 in rows.iterrows():
                if not csv_file1 == csv_file2 and not r2[id_column1] == rid:
                    matches.add(r2[id_column1])
            if long_matches is None:
                long_matches = rows
            else:
                long_matches = pd.concat([long_matches, rows])
    long_matches[[id_column1, name_column1, id_column2, name_column2]].to_csv(path_or_buf=csv_outputfile,sep="\t")
    #df1.loc[df1[id_column1].isin(matches)].to_csv(path_or_buf=csv_outputfile)
    return long_matches[[id_column1, name_column1, id_column2, name_column2]]


def remove_duplicates(csv_file1, csv_file2, id_column1, id_column2, name_column1, name_column2, csv_outputfile):


    matches = batch_match(csv_file1, csv_file2, id_column1, id_column2, name_column1, name_column2, csv_outputfile)
    print("Removing duplicates")
    id_column1_2 = "x_" + id_column1
    id_column2_2 = "y_" + id_column2
    final_mappings = dict()
    duplicate_ctr = 0
    for index, row in matches.iterrows():
        srcid = row[id_column1_2]
        tgtid = row[id_column2_2]
        if srcid not in final_mappings.keys():
                    final_mappings[srcid] = srcid
                    final_mappings[tgtid] = srcid
                    duplicate_ctr = duplicate_ctr + 1
        else:
                    final_mappings[tgtid] = final_mappings[srcid]
                    duplicate_ctr = duplicate_ctr + 1

    df = pd.read_csv(csv_file1, sep=',', error_bad_lines=False, encoding = "ISO-8859-1")
    df_new = pd.DataFrame(columns=df.columns)
    for index, row in df.iterrows():
        if row[id_column1] in final_mappings.keys():
            if not final_mappings[row[id_column1]] == row[id_column1]:
                continue
        df_new = df_new.append(row.to_frame().transpose(), ignore_index=True)

    df_new.to_csv(path_or_buf=csv_file1)

    print("Done.")

if __name__ == '__main__':
    #csv_file1 = "C:/Users/D072202/RData2Graph/rdata2graph/data/sap_hilti_data/P"
    #csv_file2 = "C:/Users/D072202/RData2Graph/rdata2graph/data/sap_hilti_data/Products"
    #name_column1 = "MARA_FERT.MAKTX;;"
    #name_column2 = 'Products.name;;'
    #id_column1 = "MARA_FERT.id;id;x"
    #csv_outputfile = 'C:/Users/D072202/RData2Graph/rdata2graph/data/sap_hilti_data/MARA_FERT3'
    #batch_match(csv_file1, csv_file2, id_column1, name_column1, name_column2, csv_outputfile)

    csv_file1 = "C:/Users/D072202/RData2Graph/rdata2graph/data/sap_hilti_data/MARA_FERT"
    csv_file2 = "C:/Users/D072202/RData2Graph/rdata2graph/data/sap_hilti_data/MARA_FERT"
    name_column1 = "MARA_FERT.MAKTX;;"
    name_column2 = "MARA_FERT.MAKTX;;"#'Products.name;;'
    id_column1 = "MARA_FERT.id;id;x"
    id_column2 = "MARA_FERT.id;id;x"#"Products.id;id;x"
    csv_outputfile = 'C:/Users/D072202/RData2Graph/rdata2graph/data/sap_hilti_data/maps'
    batch_match(csv_file1, csv_file2, id_column1, id_column2, name_column1, name_column2, csv_outputfile)

    #csv_file1 = "C:/Users/D072202/RData2Graph/rdata2graph/data/sap_hilti_data/Products"
    #csv_file2 = "C:/Users/D072202/RData2Graph/rdata2graph/data/sap_hilti_data/Products"
    #name_column1 = "Products.name;;"
    #name_column2 = "Products.name;;"#'Products.name;;'
    #id_column1 = "Products.id;id;x"
    #id_column2 = "Products.id;id;x"#"Products.id;id;x"
    #csv_outputfile = 'C:/Users/D072202/RData2Graph/rdata2graph/data/sap_hilti_data/maps'
    #remove_duplicates(csv_file1, csv_file2, id_column1, id_column2, name_column1, name_column2, csv_outputfile)
