
# coding: utf-8

# In[133]:


from gensim.models import Doc2Vec, Word2Vec
import numpy as np
from sklearn.metrics.pairwise import cosine_similarity
import editdistance
from sklearn.metrics.pairwise import euclidean_distances
import pandas as pd

if __name__== "__main__":

    DOCVEC = False

    # In[270]:
    additional_features = None
    progress = 0

    def mergedf(df1, df2):
        if df1 is None:
            return df2
        else:
            return df1.append(df2, ignore_index=True)

    basedir = 'C:/Users/D072202/DeepAnyMatch/DeepAnyMatch/result_data/'
    current_process_dir = basedir + "current_process/"
    dirpath = basedir+"OAEI_short_walk_much_context_2019_06_06_17_59_18_397561/"
    possible_matches_path = 'C:/Users/D072202/DeepAnyMatch/DeepAnyMatch/data/oaei_data/possible_matches.csv'
    #'C:/Users/D072202/DeepAnyMatch/DeepAnyMatch/data/sap_hilti_data/balanced_walks/possible_matches.csv'
    if DOCVEC:
        model = Doc2Vec.load(dirpath+"w2v.model")
    else:
        model = Word2Vec.load(dirpath+"w2v.model")
    documents_ids_A = dict()
    documents_ids_B = dict()
    #training_corpus = dict()
    if DOCVEC:
        with open(dirpath+"document_ids.csv", encoding="UTF-8") as f:
            for line in f:
                if '/hilti_erp/' in line.split(",")[0]:
                    documents_ids_A[int(line.split(",")[1])] = str(line.split(",")[0])
                else:
                    documents_ids_B[int(line.split(",")[1])] = str(line.split(",")[0])
    ctr = 0
    #with open(dirpath+"w2v_training_material.csv") as f:
#        for line in f:
            #training_corpus[ctr] = line.split(' ')
            ##ctr += 1

    possible_matches = dict()
    all_nodeids = set()
    with open(possible_matches_path, encoding="UTF-8") as f:
        for line in f:
            line = line.replace("\n","").split("\t")
            all_nodeids.add(line[0])
            if line[0] in possible_matches.keys():
                possible_matches[line[0]].add(line[1])
            else:
                possible_matches[line[0]] = set([line[1]])

            if line[1] in possible_matches.keys():
                possible_matches[line[1]].add(line[0])
            else:
                possible_matches[line[1]] = set([line[0]])


    resourceclasses = pd.read_csv(dirpath+'stratified_embeddings.csv')
    resources = resourceclasses.loc[~(resourceclasses.category=='http://www.w3.org/1999/02/22-rdf-syntax-ns#property') & ~(resourceclasses.category=='http://www.w3.org/2002/07/owl#class')]
    classes = resourceclasses.loc[(resourceclasses.category=='http://www.w3.org/2002/07/owl#class')]
    properties = resourceclasses.loc[(resourceclasses.category=='http://www.w3.org/1999/02/22-rdf-syntax-ns#property')]
    resources.head()

    # In[271]:


    def get_possible_matches(nid):
        if DOCVEC:
            out = list()
            for index, key in documents_ids_B.items():
                if key in possible_matches[nid]:
                    out.append(index)
            return out
        else:
            final_matches = list()
            matches = list(possible_matches[nid])
            #if nid in resources.label.tolist():
        #        for m in matches:
    #                if m in resources.label.tolist():
##                        final_matches.append(m)

            if nid in classes.label.tolist():
                for m in matches:
                    if m in classes.label.tolist():
                        final_matches.append(m)

            if nid in properties.label.tolist():
                for m in matches:
                    if m in properties.label.tolist():
                        final_matches.append(m)

            return final_matches


    # In[311]:


    total = len(all_nodeids)
    matchings = None
    with open(dirpath+'additional_features.csv', mode="w+", encoding="UTF-8") as f:
        for nodeid in all_nodeids:

            #nodeid = 'http://rdata2graph.sap.com/hilti_erp/torx_soc_re_scr_sn65_3x25_black#63ca4fe8-20ca-4115-9b52-05ca0bed27d3'
            #nodeid = 'http://rdata2graph.sap.com/hilti_erp/case_te_40___typ_037#bb007577-546b-4906-9fc9-4830fba5c0a9'
            if DOCVEC:
                for index, value in documents_ids_A.items():
                    if value == nodeid:
                        i=index

            #print("Targeted "+documents_ids_A[i] + " at i="+str(i))

            progress += 1
            if len(get_possible_matches(nodeid))<1:
                continue

            # In[312]:



            #model.docvecs.most_similar(0)


            # In[313]:


            #print('Closest in general:')
            #for val in model.docvecs.most_similar(i):
            #    try:
            #        print(documents_ids_A[int(val[0])])
            #    except:
            #        try:
            #            print(documents_ids_B[int(val[0])])
            #        except:
            #            print(str(val[0]) + " not found")


            # In[314]:


            #print('Closest in terms of cosine similarity:')
            #vecs = model.docvecs.doctag_syn0[np.array(get_possible_matches(nodeid))]
            vecs = model.wv[get_possible_matches(nodeid)]
            x = cosine_similarity(np.array([model.wv[nodeid]]), vecs)
            x = np.concatenate((x, np.array([get_possible_matches(nodeid)])), axis=0)
            sorted_x = pd.DataFrame(x).T.sort_values(by=[0], ascending=False)
            sorted_x.loc[:,'cos_score'] = 0
            ctr = 1
            sorted_x.columns = ['cos_sim' if col==0 else col for col in sorted_x.columns]
            for index, row in sorted_x.iterrows():
                #print(row[1] + " - " + str(row['cos_sim']))
                sorted_x.loc[index, 'cos_score'] = row['cos_score'] + 1/ctr
                ctr += 1


            # In[315]:


            #print('Closest in terms of Euclidean distance:')print('Closest in terms of Euclidean distance:')
            sorted_x2 = sorted_x
            vecs = model.wv[get_possible_matches(nodeid)]
            x = euclidean_distances(np.array([model.wv[nodeid]]), vecs)
            x = np.concatenate((x, np.array([get_possible_matches(nodeid)])), axis=0)
            sorted_x = pd.DataFrame(x).T.sort_values(by=[0], ascending=True)
            sorted_x.loc[:,'euclid_score'] = 0
            ctr = 1
            sorted_x.columns = ['euclid_sim' if col==0 else col for col in sorted_x.columns]
            for index, row in sorted_x.iterrows():
                #print(row[1] + " - " + str(row['euclid_sim']))
                sorted_x.loc[index, 'euclid_score'] = row['euclid_score'] + 1/ctr
                ctr += 1



            #print('Closest in terms of syntax:')
            sorted_x3 = sorted_x
            vecs = model.wv[get_possible_matches(nodeid)]
            def edits(v1, v2s):
                res = list()
                v1 = v1.split("/")[-1]
                for v2 in v2s:
                    v2 = v2.split("/")[-1]
                    res.append(editdistance.eval(v1, v2)/min(len(v1), len(v2)))
                return np.array([res])
            x = edits(nodeid, get_possible_matches(nodeid))
            x = np.concatenate((x, np.array([get_possible_matches(nodeid)])), axis=0)
            sorted_x = pd.DataFrame(x).T.sort_values(by=[0], ascending=True)
            sorted_x.loc[:,'syntax_score'] = 0
            ctr = 1
            sorted_x.columns = ['syntax_diff' if col==0 else col for col in sorted_x.columns]
            for index, row in sorted_x.iterrows():
                #print(row[1] + " - " + str(row['syntax_diff']))
                sorted_x.loc[index, 'syntax_score'] = row['syntax_score'] + 1/ctr
                ctr += 1


            # In[316]:


            #print('Closest in sum:')
            x = sorted_x3.merge(sorted_x2.merge(sorted_x, left_on=[1], right_on=[1]), left_on=[1], right_on=[1])
            x.loc[:,'total_score'] = x['cos_score'] + 2.05*x['syntax_score']  + x['euclid_score']
            sorted_x = x.sort_values(by=['total_score'], ascending=False)
            sorted_x.columns = ['tgt_id' if col==1 else col for col in sorted_x.columns]
            for index, row in sorted_x.loc[sorted_x.total_score == max(sorted_x.total_score.values),:].iterrows():
                matching_pair = pd.DataFrame([sorted_x.iloc[index]])
                matching_pair.loc[:,'src_id'] = nodeid
                #print(nodeid + "\t" + row[1] + "\t" + str(row['total_score']) + "\t" + str(row['cos_score']) + "\t" + str(row['euclid_score']))
                matchings = mergedf(matchings, matching_pair)

            print("Done " + str(int(100*progress/total)) + "%.", end='\r')


    import shutil
    try:
        os.remove(current_process_dir+"possible_matches.csv-strcombined.csv")
        os.remove(current_process_dir+"possible_matches.csv-strcombined_ids.csv")
        os.remove(current_process_dir+"additional_features.csv")
        os.remove(dirpath+"additional_features.csv")
    except:
        pass

    matchings.to_csv(dirpath+"additional_features.csv")
    matchings = matchings.sort_values(by=['syntax_diff'], ascending=True)
    married_matchings = None
    ctr = 0
    while len(matchings) > 0:
            ctr += 1
            row = matchings.head(1)
            married_matchings = mergedf(married_matchings, pd.DataFrame(row))
            matchings = matchings.loc[~(matchings.src_id == row.src_id.values[0]) & ~(matchings.tgt_id == row.tgt_id.values[0])]
    married_matchings.to_csv(dirpath+"matchings.csv")
    shutil.copy(dirpath + "matchings.csv" ,current_process_dir+"matchings.csv")
    shutil.copy(dirpath + "possible_matches.csv-strcombined.csv" ,current_process_dir+"possible_matches.csv-strcombined.csv")
    shutil.copy(dirpath + "possible_matches.csv-strcombined_ids.csv" ,current_process_dir+"possible_matches.csv-strcombined_ids.csv")
    shutil.copy(dirpath + "additional_features.csv" ,current_process_dir+"additional_features.csv")

    print("Done")
