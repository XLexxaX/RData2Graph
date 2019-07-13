


categories = dict()
idxctr = 1
categories["['Home']"] = idxctr-1


catfile = open("Categories", "w+", encoding='UTF-8')
catfile.write("Categories.id;id;x,Categories.id;supercategory;,Categories.name;;,Categories.description;;")
prodfile = open("Products", "w+", encoding='UTF-8')
prodfile.write("Products.id;id;x,Products.pid;;,Categories.id;cid;,Products.name;;,Products.description;;")
f = open("test.txt","r", encoding='UTF-8')
catfile.write("\n")
for line in f:
    line = line.split(";")
    if (line[0]=="CATEGORY"):
        if line[1] not in categories.keys():
            categories[line[1]] = idxctr
            idxctr = idxctr + 1
        l = str(categories[line[1]]) + "," + str(categories[line[3]]) + "," + str(line[2]) + "," + line[4] + "\n"
        l = l.replace("\n\n","\n")
        # ID-nr ; category-FK ; label ; description
        catfile.write(l)
f.close()
prodfile.write("\n")
idxctr = 0
f = open("test.txt","r", encoding="UTF-8")
for line in f:
    line = line.split(";")
    if (line[0]=="PRODUCT"):
        # ID-nr ; category-FK ; label ; description
        try:
            l = str(idxctr) + "," + str(line[3]) + "," + str(categories[line[4]]) + "," + str(line[2]) + "," + line[5] + "\n"
            l = l.replace("\n\n","\n")
            prodfile.write(l)
        except:
            print("Line '" + ";".join(line) + "' skipped.")
        idxctr = idxctr + 1

catfile.flush()
catfile.close()
prodfile.flush()
prodfile.close()
