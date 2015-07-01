__author__ = 'gvanbavi'

import urllib2
import random
import os
import bs4
import httplib
import langid
import re
import numpy
import string
from collections import Counter
import multiprocessing as mp


## extractor class

def agents():
    user_agents = [
        'Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2049.0 Safari/537.36',
        'Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.67 Safari/537.36',
        'Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)',
        'Mozilla/5.0 (compatible; Konqueror/3.5; Linux) KHTML/3.5.5 (like Gecko) (Kubuntu)',
        'Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.0.12) Gecko/20070731 Ubuntu/dapper-security Firefox/1.5.0.12',
        'Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.16 Safari/537.36'
    ]

    agent=random.choice(user_agents)

    return agent

def gettinglan(text):

    lanre=langid.classify(text)
    language= lanre[0]
    certainty=lanre[1]

    return language,certainty

def download(url):

    passed=False
    raw=""

    try:
        agent=agents()
        req = urllib2.Request(url, None, {'User-agent': agent})
        data = urllib2.urlopen(req)
        html= data.read()
        raw = bs4.BeautifulSoup(html,"lxml")

        [s.extract() for s in raw(['style', 'script', '[document]', 'head', 'title'])]
        text=raw.get_text()#.encode('utf-8')

        if (gettinglan(text)[0] in ['en']) and (gettinglan(text)[1]>0.99):

            filterd=filter(lambda x: x in string.printable, ''.join([i for i in text.encode('ascii','ignore').lower() if not i.isdigit()]))
            filterd=re.sub(r'[^a-zA-Z0-9 ]',r' ',filterd)
            counter=Counter(filterd.replace("\n","").replace(" ",""))
            values=sorted(counter.iteritems())
            suml=[]
            for value in values:
                suml.append(value[1])
            sum=numpy.sum(suml)
            if sum>200:
                passed=True


    except urllib2.HTTPError, e:
         print "ERROR -> skipping website, went into except HTTPError",e.code, url
    except urllib2.URLError, e:
         print "ERROR -> skipping website, went into except UrlError",e.reason, url
    except httplib.HTTPException, e:
         print "ERROR -> skipping website, went into except HTTPException", url
    except Exception:
         print "ERROR -> skipping website, went into except Exception", url


    return raw,passed

def linkshort(link):
    link=link.encode('utf-8')
    link=urllib2.quote(link)
    link=link.replace("%3A",":").replace("%C2%A0%C2%A0%C2%A0%C2%A0","").replace("%5EM","").replace("%0D","")
    name=link.strip("://.").replace("http://","").replace("/","")

    return link,name

def linkchecker(keyword,link,name):

        raw=""

        #if len(link.split("/"))<5:
        #    raw,accept=download(link)
        if "wikipedia" not in link:
            raw,accept=download(link)
        else:
            accept=False

        if (keyword!="Blog") and ("blog" in link):
            accept=False



        return accept,link,name,raw

def extract_data_from(keyword,listoflink,number,path2save,df):

        go=True
        page=0
        links=[]
        count=0
        agent=agents()
        ###getting number
        address="http://www.dmoz.org/search?q="+keyword+"&start=0"
        response = urllib2.Request(address, None, {'User-agent': agent})
        html = urllib2.urlopen(response).read()
        lines=html.split("\n")
        for line in lines:
            if "<small" in line:

                numberf=line.split()[2].split(")")[0]


        tvs=open("cvs_"+keyword.replace(" ","")+".txt","w")

        while page<int(number):


            keywords=keyword.replace(" ","+")
            address="http://www.dmoz.org/search?q="+keywords+"&start="+str(page)


            response=urllib2.Request(address, None, {'User-agent': agent})
            html= urllib2.urlopen(response).read()

            raw = bs4.BeautifulSoup(html)
            raw.get_text().encode('utf-8')
            data=raw.find_all(text=True)

            for da in data:
                if ("http://" in da) and ("location.protocol.toLowerCase" not in da):

                    link=da.replace("--","").replace(" ","").replace("\n","")
                    link,name=linkshort(link)
                    if (name not in listoflink):
                        accept,link,name,raw=linkchecker(keyword,link,name)
                        if (accept==True):
                            print >> tvs,link,"\t",keyword,"\t",name
                            dfile=open(path2save+"/"+keyword.replace(" ","")+"_"+name+".txt","w")
                            print >>  dfile,raw
                            dfile.close()
                            listoflink.append(name)
                            count=count+1
                        else:
                            print >>df,name

            page=page+20

            if count==numberf:
                print "INFO -> Finished with",count
                number=numberf
                break



        tvs.close()

def processrunning(pro):
    temp=[]
    for p in pro:

        if p.is_alive():
            temp.append(p)

    return temp


factor=100
ncpus=mp.cpu_count()*factor
types=open("../scikit/types","r").readlines()
keywords=[]
listoflink=[]
for line in types:

    if line.replace("\n","").replace(" ","").isalpha():
        keywords.append(line.replace("\n",""))

path2save="./data/category/"
procc=[]
count=0

ignore=[]
df=open("./ignore.txt","r")

for line in df.readlines():
    ignore.append(line.replace("\n","").replace(" ",""))

listoflink.extend(ignore)
df.close()
df=open("./ignore.txt","a")
keywords=["Wiki"]
for keyword in keywords:

    path2savekey=path2save+keyword.replace(" ","")+"/"
    if os.path.exists(path2savekey)==False:
        os.mkdir(path2savekey)
    files=os.listdir(path2savekey)
    tempp=[]
    for file in files:
        dd=file.split("_")[1].replace(".txt","")
        tempp.append(dd)

    listoflink.extend(tempp)
    print "INFO -> will run for keyword",keyword
    p=mp.Process(target=extract_data_from,args=(keyword,listoflink,2500,path2savekey,df))
    p.start()
    #if len(processrunning(procc))>ncpus:
    #p.join()

    #procc.append(p)


