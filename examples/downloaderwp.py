__author__ = 'gvanbavi'

##imports
import multiprocessing as mp
import urllib2
import bs4
import random
import os
import httplib
from collections import Counter
import numpy
import psutil
import time

## downloader class
class downloader(object):
    def __init__(self):
        print "INFO ->> Downloader class initialised"


    def agents(self):
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

    def downloadhtml2text(self,link,keyword,path,name):

        passed=False

        path=path+"/"+keyword
        if os.path.exists(path)==False:
            os.mkdir(path)

        try:
            savefile=path+"/"+keyword+"_"+name+".txt"
            if os.path.exists(savefile)==False:
                agent=self.agents()
                req = urllib2.Request(link, None, {'User-agent': agent})
                data = urllib2.urlopen(req)
                html= data.read()
                raw = bs4.BeautifulSoup(html,"lxml")

                passed=True


        except urllib2.HTTPError, e:
             print "ERROR -> skipping website, went into except HTTPError",e.code, link
        except urllib2.URLError, e:
             print "ERROR -> skipping website, went into except UrlError",e.reason, link
        except httplib.HTTPException, e:
             print "ERROR -> skipping website, went into except HTTPException", link
        except Exception:
             print "ERROR -> skipping website, went into except Exception", link


        if passed==True:

                ffile=open(savefile,"w")
                print >> ffile,raw
                ffile.close()


    def preparelines(self,keyword,path2save,line):

        linesp=line.split()[0]
        if "http" in linesp:
                link=urllib2.quote(linesp)
                link=link.replace("%3A",":").replace("%C2%A0%C2%A0%C2%A0%C2%A0","").replace("%5EM","")
                name=link.strip("://.").replace("http://","").replace("/","")

                self.downloadhtml2text(link,keyword,path2save,name)

        return link,name











#downloader().downloadhtml2text("http://webprofits.com.au","test","./data/category/",0)




