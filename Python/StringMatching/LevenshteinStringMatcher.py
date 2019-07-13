from StringMatching.GraphToolbox import GraphManager
from StringMatching.InvertedIndexToolbox import getNGrams
import operator
import pprint
import os

class StringMatcher:

    def __init__(self, src_file_path, tgt_file_path):
        self.src_graphmanager = GraphManager()
        self.src_graphmanager.setIndexProperties(index_properties)
        self.src_graphmanager.readGraphFromNTFile(src_file_path)
        self.tgt_graphmanager = GraphManager()
        self.tgt_graphmanager.setIndexProperties(index_properties)
        self.tgt_graphmanager.readGraphFromNTFile(tgt_file_path)

    def preciseBatchMatch(self, path, min_similarity):
        f = open(path, "a+", encoding="UTF-8")
        correspondences = dict()
        # iterate nodes
        ctr = 0
        for id, node in self.src_graphmanager.graph.items():
            ctr = ctr + 1
            if ctr > 100000:
                print("Done")
                return correspondences
            if ctr % 100 == 0:
                print("Compared " + str(ctr) )
            correspondences[id] = list()
            for id2, node2 in self.tgt_graphmanager.graph.items():
                txt1 = ""
                txt2 = ""
                for ind in index_properties:
                    if ind in node.keys():
                        txt1 = txt1 + node[ind]
                    if ind in node2.keys():
                        txt2 = txt2 + node2[ind]
                if len(txt1) > 20 and len(txt2) > 20:
                    if ((self.iterative_levenshtein(txt1, txt2)-(abs(len(txt1)-len(txt2))))/min(len(txt1), len(txt2))) < (1-min_similarity):
                            correspondences[id] = correspondences[id] + [id2]
                            f.write(txt1 + " -> " + txt2 + " <----> " + str(id).replace("<","").replace(">","") + "\t" + str(id2).replace("<","").replace(">","") + "\n")
                            f.flush()

        f.close()
        return correspondences

    def iterative_levenshtein(self, s, t):
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

    def formatted_save(self, mappings, path):
        f = open(path, "a+")
        mappings_ctr = 0
        for key1, key2 in mappings.items():
            if key2 is not None:
                if len(key2) > 0:
                    mappings_ctr = mappings_ctr + 1
                    txt1 = ""
                    txt2 = ""
                    for p in index_properties:
                        try:
                            txt1 = self.src_graphmanager[key1][p]
                        except:
                            pass
                    for p in index_properties:
                        try:
                            txt2 = self.tgt_graphmanager[key2][p]
                        except:
                            pass
                    f.write(txt1 + " -> " + txt2 + " <----> " + str(key1).replace("<","").replace(">","") + "\t" + str(key2).replace("<","").replace(">","") + "\n")
        f.close()
        print("Found " + str(mappings_ctr) + " mappings.")


if __name__ == '__main__':
    try:
        os.remove("C:/Users/D072202/RData2Graph/rdata2graph/data/sap_hilti_data/sap_hilti_gold.csv")
    except FileNotFoundError:
        pass

    # match products
    index_properties = ["<http://rdata2graph.sap.com/hilti_erp/property/MARA_FERT.MAKTX>".lower(), "<http://rdata2graph.sap.com/hilti_web/property/Products.name>".lower()]
    stringmatcher = StringMatcher("C:/Users/D072202/RData2Graph/rdata2graph/data/sap_hilti_data/graph_triples_hilti_erp.nt","C:/Users/D072202/RData2Graph/rdata2graph/data/sap_hilti_data/graph_triples_hilti_web.nt")
    mappings = stringmatcher.preciseBatchMatch("C:/Users/D072202/RData2Graph/rdata2graph/data/sap_hilti_data/sap_hilti_gold.csv", 0.7)
    pprint.PrettyPrinter().pprint(str(mappings))

    # match categories
    #index_properties = ["<http://rdata2graph.sap.com/hilti_erp/property/T179.Description>".lower(), "<http://rdata2graph.sap.com/hilti_web/property/Categories.name>".lower()]
    #stringmatcher = StringMatcher("C:/Users/D072202/RData2Graph/rdata2graph/data/sap_hilti_data/full_strings/graph_triples_hilti_erp.nt","C:/Users/D072202/RData2Graph/rdata2graph/data/sap_hilti_data/full_strings/graph_triples_hilti_web.nt")
    #mappings = stringmatcher.preciseBatchMatch("C:/Users/D072202/RData2Graph/rdata2graph/data/sap_hilti_data/sap_hilti_gold.csv", 0.7)
    #pprint.PrettyPrinter().pprint(str(mappings))
