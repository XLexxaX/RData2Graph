from GraphToolbox import GraphManager
from InvertedIndexToolbox import getNGrams
import operator
import pprint
import random
import os
import pandas as pd

class StringMatcher:

    def __init__(self, src_file_path, tgt_file_path):
        self.src_graphmanager = GraphManager()
        self.src_graphmanager.setIndexProperties(index_properties)
        self.src_graphmanager.readGraphFromNTFile(src_file_path)
        self.tgt_graphmanager = GraphManager()
        self.tgt_graphmanager.setIndexProperties(index_properties)
        self.tgt_graphmanager.readGraphFromNTFile(tgt_file_path)




    def batchMatch(self):
        correspondences = dict()
        for nodeid in self.src_graphmanager.graph.keys():
            indices = []
            for src_prop in self.src_graphmanager.indices.keys():
                for tgt_prop in self.tgt_graphmanager.indices.keys():
                    try:
                        indices = indices + self.tgt_graphmanager.indices[tgt_prop].getIndicesForValue(self.src_graphmanager.graph[nodeid][src_prop])
                    except KeyError:
                        pass
            tmp_tgt_ind = dict()
            for index in indices:
                if index in tmp_tgt_ind.keys():
                    tmp_tgt_ind[index] = tmp_tgt_ind[index] + 1
                else:
                    tmp_tgt_ind[index] = 1
            if len(tmp_tgt_ind)<1:
                correspondences[nodeid] = None
            else:
                correspondences[nodeid] = max((tmp_tgt_ind.items()), key=operator.itemgetter(1))[0]
        return correspondences

    def preciseBatchMatch(self, path, pm_dir, gm_dir, min_similarity, max_similarity=1.0):
        print('Starting matching')

        gold_mapping_path = gm_dir
        gold_mapping = pd.read_csv(gold_mapping_path, sep='\t', encoding="UTF-8", header=None, names=['src_id', 'tgt_id', 'label'])
        f = open(pm_dir,'w+', encoding="UTF-8")
        for index, row in gold_mapping.iterrows():

            #if row['src_id'] == 'http://rdata2graph.sap.com/darkscape/#aae029bf-923b-482e-8d7b-85dc6986277e':
            #                print("hoho")
            if str(row['tgt_id']).lower() == str(float('NaN')) and row['label'] == 0:
                    try:
                        res = self.match_src("<"+row['src_id']+">")
                        if res is not None:
                            f.write((str(res).replace("\n","\t"+str(0)+"\n")+"\n").replace("\n\n","\n"))
                        else:
                            print(str(row['src_id']) + " matched to none.")
                    except KeyError:
                        pass
                    #{'src_id': row['src_id'], 'tgt_id': descriptor, 'label': row['label']}
            elif str(row['src_id']).lower() == str(float('NaN')) and row['label'] == 0:
                    try:
                        res = self.match_tgt("<"+row['tgt_id']+">")
                        if res is not None:
                            f.write((str(res).replace("\n","\t"+str(0)+"\n")+"\n").replace("\n\n","\n"))
                        else:
                            print(str(row['tgt_id']) + " matched to none.")
                    except KeyError:
                        pass
            else:
                found = False
                try:
                        res = self.match_tgt("<"+row['tgt_id']+">", exclude_nodeid="")
                        if row['src_id'] in res and row['label'] == 1:
                            found = True
                            f.write(str(row['src_id']) + "\t" + str(row['tgt_id'])+"\t"+str(row["label"])+"\n")
                            res = str(res).replace(str(row['src_id']) + "\t" + str(row['tgt_id']) + "\n","")
                        if res is not None:
                            f.write((str(res).replace("\n","\t"+str(0)+"\n")+"\n").replace("\n\n","\n"))
                        else:
                            print(str(row['tgt_id']) + " matched to none.")
                except:
                        pass
                try:
                        res = self.match_src("<"+row['src_id']+">", exclude_nodeid="")
                        if not found and row['tgt_id'] in res and row['label'] == 1:
                            f.write(str(row['src_id']) + "\t" + str(row['tgt_id'])+"\t"+str(row["label"])+"\n")
                            res = str(res).replace(str(row['src_id']) + "\t" + str(row['tgt_id']) + "\n","")
                        else:
                            res = str(res).replace(str(row['src_id']) + "\t" + str(row['tgt_id']) + "\n","")
                        if res is not None:
                            f.write((str(res).replace("\n","\t"+str(0)+"\n")+"\n").replace("\n\n","\n"))
                        else:
                            print(str(row['src_id']) + " matched to none.")
                except:
                        pass


        f.close()

        lines = dict()
        with open(pm_dir,mode="r", encoding="UTF-8") as f:
                for line in f:
                    if not line == "\n":
                        line = line.split("\t")
                        line[2] = str(line[2]).replace("\n","")
                        if not line[0] + "\t" + line[1] in lines.keys():
                            lines[line[0] + "\t" + line[1]] = float(line[2])
                        elif line[0] + "\t" + line[1] in lines.keys() and float(line[2])==1.0:
                            lines[line[0] + "\t" + line[1]] = float(line[2])
        with open(pm_dir,mode="w+", encoding="UTF-8") as f2:
            for line, target in lines.items():
                f2.write(line + "\t" + str(target)+"\n")

    def match_tgt(self, nodeid, min_similarity=0.01, max_similarity=1.0, exclude_nodeid=None):
            indices = []
            for tgt_prop in self.tgt_graphmanager.indices.keys():
                for src_prop in self.src_graphmanager.indices.keys():
                    try:
                        self.src_graphmanager.indices[src_prop]
                        self.tgt_graphmanager.graph[nodeid]
                        indices = indices + self.src_graphmanager.indices[src_prop].getIndicesForValue(self.tgt_graphmanager.graph[nodeid][tgt_prop])
                    except KeyError:
                        pass
            if exclude_nodeid is not None and exclude_nodeid in indices:
                indices.remove(exclude_nodeid)
            tmp_tgt_ind = dict()
            for index in indices:
                if index in tmp_tgt_ind.keys():
                    tmp_tgt_ind[index] = tmp_tgt_ind[index] + 1
                else:
                    tmp_tgt_ind[index] = 1



            if len(tmp_tgt_ind)<1:
                return None
            else:
                out = ""
                import heapq
                best_matching_resources = heapq.nlargest(1,tmp_tgt_ind.items(), key=operator.itemgetter(1)) #max((tmp_tgt_ind.items()), key=operator.itemgetter(1))
                for best_matching_resource in best_matching_resources:
                    maximum_ngrams_x = 0
                    maximum_ngrams_y = 0
                    for tgt_prop in self.tgt_graphmanager.indices.keys():
                        try:
                            maximum_ngrams_x = maximum_ngrams_x + len(getNGrams(self.tgt_graphmanager.graph[nodeid][tgt_prop]))
                        except KeyError:
                            pass
                    for src_prop in self.src_graphmanager.indices.keys():
                        try:
                            maximum_ngrams_y = maximum_ngrams_y + len(self.src_graphmanager.graph[best_matching_resource[0]][src_prop])
                        except KeyError:
                            pass
                    no_of_ngrams = max(maximum_ngrams_x, maximum_ngrams_y)
                    if best_matching_resource[1] > no_of_ngrams*min_similarity and best_matching_resource[1] < no_of_ngrams and best_matching_resource[1] <= min(maximum_ngrams_x, maximum_ngrams_y)*max_similarity:
                        try:
                                out = out + str(best_matching_resource[0]).replace("<","").replace(">","") + "\t" + nodeid.replace("<","").replace(">","") +"\n"
                                #l = self.src_graphmanager.graph[nodeid]["<http://rdata2graph.sap.com/darkscape/non-player_character.label>".lower()] + " -> " + self.tgt_graphmanager.graph[best_matching_resource[0]]["<http://rdata2graph.sap.com/oldschoolrunescape/non-player_character.label>".lower()] + " <----> " + str(nodeid).replace("<","").replace(">","") + "\t" + str(best_matching_resource[0]).replace("<","").replace(">","") + "\r\n"
                                #l = str(nodeid).replace("<","").replace(">","") + "\t" + str(best_matching_resource[0]).replace("<","").replace(">","") + "\r\n"
                                #f.write(l.lower())
                                #f.flush()
                        except KeyError:
                            pass
                return out



    def match_src(self, nodeid, min_similarity=0.01, max_similarity=1.0, exclude_nodeid=None):
            indices = []
            for tgt_prop in self.tgt_graphmanager.indices.keys():
                for src_prop in self.src_graphmanager.indices.keys():
                    try:
                        self.tgt_graphmanager.indices[tgt_prop]
                        indices = indices + self.tgt_graphmanager.indices[tgt_prop].getIndicesForValue(self.src_graphmanager.graph[nodeid][src_prop])
                    except KeyError:
                        pass
            if exclude_nodeid is not None and exclude_nodeid in indices:
                indices.remove(exclude_nodeid)
            tmp_tgt_ind = dict()
            for index in indices:
                if index in tmp_tgt_ind.keys():
                    tmp_tgt_ind[index] = tmp_tgt_ind[index] + 1
                else:
                    tmp_tgt_ind[index] = 1



            if len(tmp_tgt_ind)<1:
                return None
            else:
                out = ""
                import heapq
                best_matching_resources = heapq.nlargest(10,tmp_tgt_ind.items(), key=operator.itemgetter(1)) #max((tmp_tgt_ind.items()), key=operator.itemgetter(1))
                for best_matching_resource in best_matching_resources:
                    maximum_ngrams_x = 0
                    maximum_ngrams_y = 0
                    for src_prop in self.src_graphmanager.indices.keys():
                        try:
                            maximum_ngrams_x = maximum_ngrams_x + len(getNGrams(self.src_graphmanager.graph[nodeid][src_prop]))
                        except KeyError:
                            pass
                    for tgt_prop in self.tgt_graphmanager.indices.keys():
                        try:
                            maximum_ngrams_y = maximum_ngrams_y + len(self.tgt_graphmanager.graph[best_matching_resource[0]][tgt_prop])
                        except KeyError:
                            pass
                    no_of_ngrams = max(maximum_ngrams_x, maximum_ngrams_y)
                    if best_matching_resource[1] > no_of_ngrams*min_similarity and best_matching_resource[1] < no_of_ngrams and best_matching_resource[1] <= min(maximum_ngrams_x, maximum_ngrams_y)*max_similarity:
                        try:
                                out = out  + nodeid.replace("<","").replace(">","")+ "\t" +str(best_matching_resource[0]).replace("<","").replace(">","") + "\n"
                                #l = self.src_graphmanager.graph[nodeid]["<http://rdata2graph.sap.com/darkscape/non-player_character.label>".lower()] + " -> " + self.tgt_graphmanager.graph[best_matching_resource[0]]["<http://rdata2graph.sap.com/oldschoolrunescape/non-player_character.label>".lower()] + " <----> " + str(nodeid).replace("<","").replace(">","") + "\t" + str(best_matching_resource[0]).replace("<","").replace(">","") + "\r\n"
                                #l = str(nodeid).replace("<","").replace(">","") + "\t" + str(best_matching_resource[0]).replace("<","").replace(">","") + "\r\n"
                                #f.write(l.lower())
                                #f.flush()
                        except KeyError:
                            pass
                return out



def formatted_save(mappings, path):
    f = open(path, "a+", encoding="UTF-8")
    mappings_ctr = 0
    for key1, key2 in mappings.items():
        if key2 is not None:
            mappings_ctr = mappings_ctr + 1
            l = str(key1).replace("<","").replace(">","") + "\t" + str(key2).replace("<","").replace(">","") + "\n"
            f.write(l.lower())
    f.close()
    print("Found " + str(mappings_ctr) + " mappings.")


if __name__ == '__main__':

    #prefixes = [["ma","dc"],
    #            ["mea","meb"], ["mea","st"],["meb","st"],
    #            ["ru","da"],["ru","ol"],["he","dc"],["ma","he"]]
    prefixes = [["he","dc"],["ma","he"]]
    for prefix in prefixes:
        basedir = "C:/Users/D072202/RData2Graph/rdata2graph/data/"+prefix[0]+prefix[1]+"/"
        pm_dir = basedir+'possible_matches.csv'
        gm_dir = basedir+prefix[0]+prefix[1]+'.csv'
        triples_1 = basedir + "graph_triples_"+prefix[0]+".nt"
        triples_2 =basedir + "graph_triples_"+prefix[1]+".nt"
        try:
            os.remove(pm_dir)
        except FileNotFoundError:
            pass

        # match products
        index_properties = list()
        with open("C:/Users/D072202/RData2Graph/rdata2graph/data/oaei_data/labels.txt", mode="r", encoding="UTF-8") as f:
            for label in f:
                if random.randint(1,101) > 0:
                    index_properties.append("<"+label.replace('\n','')+">")
            print('label size: ' + str(len(index_properties)))

        stringmatcher = StringMatcher(triples_1,triples_2)
        stringmatcher.preciseBatchMatch("", pm_dir, gm_dir, 0.0)

    #formatted_save(mappings, "/sapmnt/home/D072202/RData2Graph/rdata2graph/data/sap_hilti_data/sap_hilti_gold.csv")

    # match categories
    #index_properties = ["<http://rdata2graph.sap.com/hilti_erp/property/T179.Description>", "<http://rdata2graph.sap.com/hilti_web/property/Categories.name>"]
    #stringmatcher = StringMatcher("C:/Users/D072202/RData2Graph/rdata2graph/data/sap_hilti_data/graph_triples_hilti_erp.nt","C:/Users/D072202/RData2Graph/rdata2graph/data/sap_hilti_data/graph_triples_hilti_web.nt")
    #mappings = stringmatcher.preciseBatchMatch(0.85)
    #pprint.PrettyPrinter().pprint(str(mappings))

    #formatted_save(mappings, "C:/Users/D072202/RData2Graph/rdata2graph/data/sap_hilti_data/sap_hilti_gold.csv")
