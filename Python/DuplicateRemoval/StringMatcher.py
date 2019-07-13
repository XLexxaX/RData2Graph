from StringMatching.GraphToolbox import GraphManager
from StringMatching.InvertedIndexToolbox import getNGrams
import operator
import pprint
import os
import shutil

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


    def sortkey(self, val):
        return val[1]

    def preciseBatchMatch(self, min_similarity):
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
                best_matching_resources = list(tmp_tgt_ind.items())
                best_matching_resources.sort(key = self.sortkey, reverse = True)
                maximum_ngrams_x = 0

                for src_prop in self.src_graphmanager.indices.keys():
                    try:
                        maximum_ngrams_x = maximum_ngrams_x + len(getNGrams(self.src_graphmanager.graph[nodeid][src_prop]))
                    except KeyError:
                        pass

                for item in best_matching_resources:

                    if item[0] == nodeid:
                        continue

                    maximum_ngrams_y = 0
                    for tgt_prop in self.tgt_graphmanager.indices.keys():
                        try:
                            maximum_ngrams_y = maximum_ngrams_y + len(self.tgt_graphmanager.graph[item[0]][tgt_prop])
                        except KeyError:
                            pass

                    no_of_ngrams = min(maximum_ngrams_x, maximum_ngrams_y)
                    if item[1] > no_of_ngrams*min_similarity:
                        if nodeid not in correspondences.keys():
                            correspondences[nodeid] = list()
                        correspondences[nodeid].append(item[0])
                    else:
                        break
        return correspondences


def formatted_save(mappings, path):
    f = open(path, "a+")
    for key1, key2 in mappings.items():
        if key2 is not None:
            l = str(key1).replace("<","").replace(">","") + "\t" + str(key2).replace("<","").replace(">","") + "\n"
            f.write(l.lower())
    f.close()


def remove_duplicates(index_properties, triplespath, corpuspath):
    print("Reading data ...")
    # match products
    stringmatcher = StringMatcher(triplespath,triplespath)
    print("Matching data ...")
    mappings = stringmatcher.preciseBatchMatch(0.8)
    #pprint.PrettyPrinter().pprint(str(mappings))

    # Remove duplicate entries.
    import re
    corpusfile = open(corpuspath,"r", encoding="UTF-8")
    triplesfile = open(triplespath,"r", encoding="UTF-8")
    corpusfile_new = open(corpuspath+".tmp","w+", encoding="UTF-8")
    triplesfile_new = open(triplespath+".tmp","w+", encoding="UTF-8")

    # As we have mapped one database onto itself, we have every mapping twice in the mapping-variable.
    # Hence, we have to create a final_mappings dict, that eliminates those duplicates and
    # contains all mappings just once.
    #
    # Our final aim is to build clusters of the mappings, so that we have map all resources in that cluster to just one
    # resource in the end.
    print("Starting file correction ...")
    final_mappings = dict()
    duplicate_ctr = 0
    for srcid, tgtids in mappings.items():
        if tgtids is not None:
            for tgtid in tgtids:
                if srcid not in final_mappings.keys():
                    final_mappings[srcid] = srcid
                    final_mappings[tgtid] = srcid
                    duplicate_ctr = duplicate_ctr + 1
                else:
                    final_mappings[tgtid] = final_mappings[srcid]
                    duplicate_ctr = duplicate_ctr + 1

    for line in corpusfile:
            newline = ""
            for word in line.split(" "):
                if word in final_mappings.keys():
                    if not newline == "":
                        newline = newline + " "
                    newline = newline + final_mappings[word]
                else:
                    if not newline == "":
                        newline = newline + " "
                    newline = newline + word
            corpusfile_new.write(newline.lower())
    for line in triplesfile:
            newline = ""
            for word in line.split(" "):
                if word in final_mappings.keys():
                    if not newline == "":
                        newline = newline + " "
                    newline = newline + final_mappings[word]
                else:
                    if not newline == "":
                        newline = newline + " "
                    newline = newline + word
            triplesfile_new.write(newline.lower())

    corpusfile_new.close()
    corpusfile.close()
    triplesfile_new.close()
    triplesfile.close()

    shutil.copy(corpuspath+".tmp", corpuspath)
    shutil.copy(triplespath+".tmp", triplespath)
    os.remove(corpuspath+".tmp")
    os.remove(triplespath+".tmp")

    print("Resolved " + str(duplicate_ctr) + " duplicate resources.")


if __name__ == '__main__':
        index_properties = ["<http://rdata2graph.sap.com/hilti_erp/property/MARA_FERT.MAKTX>".lower(), "<http://rdata2graph.sap.com/hilti_erp/property/MARA_FERT.MAKTX>".lower()]
        triplespath = "C:/Users/D072202/RData2Graph/rdata2graph/data/sap_hilti_data/graph_triples_hilti_erp.nt"
        corpuspath = "C:/Users/D072202/RData2Graph/rdata2graph/data/sap_hilti_data/corpus_hilti_erp.txt"
        remove_duplicates(index_properties, triplespath, corpuspath)

        index_properties = ["<http://rdata2graph.sap.com/hilti_web/property/Products.name>".lower(), "<http://rdata2graph.sap.com/hilti_web/property/Products.name>".lower()]
        triplespath = "C:/Users/D072202/RData2Graph/rdata2graph/data/sap_hilti_data/graph_triples_hilti_web.nt"
        corpuspath = "C:/Users/D072202/RData2Graph/rdata2graph/data/sap_hilti_data/corpus_hilti_web.txt"
        remove_duplicates(index_properties, triplespath, corpuspath)
