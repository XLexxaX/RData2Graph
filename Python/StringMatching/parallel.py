from GraphToolbox import GraphManager
from InvertedIndexToolbox import getNGrams
import operator
import pprint
import random
import heapq
import StringMatcher_Interface as smi
import os

class StringMatcher:

    def __init__(self, src_file_path, tgt_file_path, index_properties):
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
        correspondences = dict()
        i=0
        total_size = len(self.src_graphmanager.graph.keys())
        current_run = 1
        allkeys = list()
        proccount=5
        partkeys = list()
        for nodeid in self.src_graphmanager.graph.keys():
            i=i+1
            if i < total_size/proccount*current_run:
                partkeys.append(nodeid)
            else:
                current_run = current_run + 1
                allkeys.append(partkeys)
                partkeys = list()
        import random
        import multiprocessing as mp
        import StringMatcher_Interface as smi
        jobs = list()
        queues = list()
        print("multithread now")
        x=0
        for keys in allkeys:
            print("Starting process " + str(x))
            x=x+1
            ctx = mp.get_context('spawn')
            q = ctx.Queue()
            process = ctx.Process(target=smi.main, args=(keys, q,x))
            jobs.append(process)
            queues.append(q)
        for j in jobs:
            j.start()
        for q in queues:
            print(q.get())
        for j in jobs:
            j.join()
        return None




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


def main():
    try:
        os.remove("../../data/oaei_data/oaei_gold_standard.csv")
    except FileNotFoundError:
        pass

    # match products
    index_properties = list()
    with open("C:/Users/D072202/RData2Graph/rdata2graph/data/oaei_data/labels.txt", mode="r", encoding="UTF-8") as f:
        for label in f:
            if random.randint(1,101) > 0:
                index_properties.append(label.replace('\n',''))
        print('label size: ' + str(len(index_properties)))

    stringmatcher = StringMatcher("../../data/oaei_data/graph_triples_darkscape.nt","../../data/oaei_data/graph_triples_oldschoolrunescape.nt", index_properties)
    mappings = stringmatcher.preciseBatchMatch("../../data/oaei_data/oaei_gold_standard.csv", 0.9)
    #pprint.PrettyPrinter().pprint(str(mappings))
    #formatted_save(mappings, "/sapmnt/home/D072202/RData2Graph/rdata2graph/data/sap_hilti_data/sap_hilti_gold.csv")

    # match categories
    #index_properties = ["<http://rdata2graph.sap.com/hilti_erp/property/T179.Description>", "<http://rdata2graph.sap.com/hilti_web/property/Categories.name>"]
    #stringmatcher = StringMatcher("C:/Users/D072202/RData2Graph/rdata2graph/data/sap_hilti_data/graph_triples_hilti_erp.nt","C:/Users/D072202/RData2Graph/rdata2graph/data/sap_hilti_data/graph_triples_hilti_web.nt")
    #mappings = stringmatcher.preciseBatchMatch(0.85)
    #pprint.PrettyPrinter().pprint(str(mappings))

    #formatted_save(mappings, "C:/Users/D072202/RData2Graph/rdata2graph/data/sap_hilti_data/sap_hilti_gold.csv")
