from GraphToolbox import GraphManager
from InvertedIndexToolbox import getNGrams
import operator
import pprint
import random
import os
import heapq

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

    def preciseBatchMatch(self, path, min_similarity, max_similarity=1.0):
        print('Starting matching')
        f = open(path, "a+", encoding="UTF-8")
        correspondences = dict()
        i=0
        total_size = len(self.src_graphmanager.graph.keys())
        for nodeid in self.src_graphmanager.graph.keys():
            i=i+1
            if i%1000==0:
                print("  " + str(int(100*i/(total_size))) + "% done")
            if random.randint(1,101) > 100:
                continue
            indices = []

            for src_prop in self.src_graphmanager.indices.keys():
                for tgt_prop in self.tgt_graphmanager.indices.keys():
                    try:
                        cind = self.tgt_graphmanager.indices[tgt_prop].getIndicesForValue(self.src_graphmanager.graph[nodeid][src_prop])
                        #if len(cind) > len(self.tgt_graphmanager.graph.keys())*0.7:
                        #    continue
                        indices = indices + cind#self.tgt_graphmanager.indices[tgt_prop].getIndicesForValue(self.src_graphmanager.graph[nodeid][src_prop])
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
                matchfound=False
                best_matching_resources = heapq.nlargest(10,tmp_tgt_ind.items(), key=operator.itemgetter(1)) #max((tmp_tgt_ind.items()), key=operator.itemgetter(1))
                #max((tmp_tgt_ind.items()), key=operator.itemgetter(1))
                for k in range(len(best_matching_resources)):
                    best_matching_resource = best_matching_resources[k]
                    lbl = 0
                    if k==0:
                        lbl=1
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
                    if k==0:
                        if no_of_ngrams > 10 and best_matching_resource[1] > no_of_ngrams*min_similarity and best_matching_resource[1] < no_of_ngrams and best_matching_resource[1] <= min(maximum_ngrams_x, maximum_ngrams_y)*max_similarity:
                            try:
                                    correspondences[nodeid] = best_matching_resource[0]
                                    #l = self.src_graphmanager.graph[nodeid]["<http://rdata2graph.sap.com/darkscape/non-player_character.label>".lower()] + " -> " + self.tgt_graphmanager.graph[best_matching_resource[0]]["<http://rdata2graph.sap.com/oldschoolrunescape/non-player_character.label>".lower()] + " <----> " + str(nodeid).replace("<","").replace(">","") + "\t" + str(best_matching_resource[0]).replace("<","").replace(">","") + "\r\n"
                                    l = str(nodeid).replace("<","").replace(">","") + "\t" + str(best_matching_resource[0]).replace("<","").replace(">","") + "\t"+str(lbl)+"\r\n"
                                    f.write(l.lower())
                                    f.flush()
                                    matchfound=True
                            except KeyError:
                                pass
                        else:
                            correspondences[nodeid] = None
                    elif no_of_ngrams > 10 and matchfound:
                            try:
                                    correspondences[nodeid] = best_matching_resource[0]
                                    #l = self.src_graphmanager.graph[nodeid]["<http://rdata2graph.sap.com/darkscape/non-player_character.label>".lower()] + " -> " + self.tgt_graphmanager.graph[best_matching_resource[0]]["<http://rdata2graph.sap.com/oldschoolrunescape/non-player_character.label>".lower()] + " <----> " + str(nodeid).replace("<","").replace(">","") + "\t" + str(best_matching_resource[0]).replace("<","").replace(">","") + "\r\n"
                                    l = str(nodeid).replace("<","").replace(">","") + "\t" + str(best_matching_resource[0]).replace("<","").replace(">","") + "\t"+str(lbl)+"\r\n"
                                    f.write(l.lower())
                                    f.flush()
                            except KeyError:
                                pass
        f.close()
        return correspondences


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


    prefixes = [["erp","web"]]
    #["he","dc"],["ma","dc"],
                #["mea","meb"], ["mea","st"],["meb","st"],
                #["ru","da"],["ru","ol"],
    prefixes =[["oldschoolrunescape","darkscape"]]
    labelspath = "C:/Users/D072202/RData2Graph/rdata2graph/data/oaei_data/labels.txt"

    for prefix in prefixes:

        basedir = "C:/Users/D072202/RData2Graph/rdata2graph/data/oaei_data/"#sap_hilti_data/"#+prefix[0]+prefix[1]+"/"
        #try:
        #    os.remove(basedir+"oaei_gold_standard.csv")
        #except FileNotFoundError:
        #    pass
        triples_1 = basedir + "graph_triples_"+prefix[0]+".nt"
        triples_2 =basedir + "graph_triples_"+prefix[1]+".nt"

        # match products
        index_properties = list()
        with open(labelspath, mode="r", encoding="UTF-8") as f:
            for label in f:
                if random.randint(1,101) > 0:
                    index_properties.append("<"+label.replace('\n','')+">")
            print('label size: ' + str(len(index_properties)))

        stringmatcher = StringMatcher(triples_1,triples_2)
        mappings = stringmatcher.preciseBatchMatch(basedir+"oaei_gold_standard1best.csv", 0.75)
        #pprint.PrettyPrinter().pprint(str(mappings))

    #formatted_save(mappings, "/sapmnt/home/D072202/RData2Graph/rdata2graph/data/sap_hilti_data/sap_hilti_gold.csv")

    # match categories
    #index_properties = ["<http://rdata2graph.sap.com/hilti_erp/property/T179.Description>", "<http://rdata2graph.sap.com/hilti_web/property/Categories.name>"]
    #stringmatcher = StringMatcher("C:/Users/D072202/RData2Graph/rdata2graph/data/sap_hilti_data/graph_triples_hilti_erp.nt","C:/Users/D072202/RData2Graph/rdata2graph/data/sap_hilti_data/graph_triples_hilti_web.nt")
    #mappings = stringmatcher.preciseBatchMatch(0.85)
    #pprint.PrettyPrinter().pprint(str(mappings))

    #formatted_save(mappings, "C:/Users/D072202/RData2Graph/rdata2graph/data/sap_hilti_data/sap_hilti_gold.csv")
