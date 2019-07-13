import scrapy
import json
import os
import re

class QuotesSpider(scrapy.Spider):
    name = "quotes"
    filename = ("test.txt").replace("/","")
    try:
        os.remove(filename)
    except:
        pass
    def start_requests(self):
        urls = [
            "https://www.hilti.com/products"
        ]
        for url in urls:
            yield scrapy.Request(url=url, callback=self.parse)

    def parse(self, response):
        page = response.url.split("/")[-2]
        filename = ("test.txt").replace("/","")

        categories_path = response.xpath("//div[@class='m-breadcrumbs-col']/ol/li/*/span/text()").extract()
        #header = (response.xpath('//div[@class="m-page-title-col"]/h1[@class="a-heading-h1 m-page-title-headline "]/text()').extract_first())

        #categories_name = response.xpath('//div/div/div/div/h1[@class="a-heading-h1 m-page-title-headline "]/text()').extract_first()
        categories_description = response.xpath('//a/div/div[2]/div[@class="m-category-item-text-description"]/text()').extract_first()

        link = response.xpath('//a[@class="m-category-item"]/@href').extract()

        if link is None or link == []:
            link = response.xpath('//a[@class="m-grid-tile--link"]/@href').extract()
            categories_description = response.xpath('//div[@class="m-page-title-col"]/p/text()').extract_first()

        products_name = response.xpath('//h1[@class="a-heading-h2 a-spacing-pb--xxs has-newtag"]/text()').extract_first()
        products_description = response.xpath('//h2[@class="a-paragraph a-spacing-pb--xxs"]/text()').extract_first()
        products_id = ""
        srch = re.search("productItemId': '\d+'", str(response.body_as_unicode()), re.IGNORECASE)
        if srch:
            products_id = str(srch.group()).split('\'')[1]
        else:
            srch = re.search("makeItFitItemID: '\d+'", str(response.body_as_unicode()), re.IGNORECASE)
            if srch:
                products_id = str(srch.group()).split('\'')[2]

        for i in range(len(categories_path)):
            categories_path[i] = self.clean(categories_path[i], categories_path)

        #try:
        with open(filename, mode='a+', encoding='UTF-8') as f:

                if link is not None and not link == []:
                    categories_name = categories_path[len(categories_path)-1]
                    clean_cat_name = self.clean(categories_name, categories_path)
                    clean_cat_description = self.clean(categories_description, categories_path)
                    # type ; ID ; label ; category-FK ; description
                    f.write("CATEGORY;" + str(categories_path) + ";" + str(clean_cat_name) + ";" + str(categories_path[0:len(categories_path)-1]) + ";" + str(clean_cat_description) + "\n")
                else:
                    clean_product_name = self.clean(products_name, categories_path)
                    clean_product_description = self.clean(products_description, categories_path)
                    #clean_product_id = self.clean(products_id, categories_path)
                    clean_product_id = products_id
                    # type ; ID ; label ; pid ; category-FK ; description
                    f.write("PRODUCT;" + str(categories_path) + ";" + str(clean_product_name) + ";" + str(clean_product_id) + ";" + str(categories_path[0:len(categories_path)-1]) + ";" + str(clean_product_description) + "\n")
            #self.log('Saved file %s' % filename)
       # except:
         #   pass

        for p in link:
                yield scrapy.Request(url="https://www.hilti.com"+p, callback=self.parse)

    def clean(self, text, path):
        try:
            return text.strip().replace("\n\s*","").replace("\s*\n","").replace("\n","").replace(";","").replace(",","")
        except:
            print("------------------------------------------------------------------------------------")
            print(str(path))
            print("------------------------------------------------------------------------------------")
