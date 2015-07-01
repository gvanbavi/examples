__author__ = 'gvanbavi'

'''
Service server for categorisation
'''
import socket
from sklearn.externals import joblib
import random
import urllib2
import langid
import sys
sys.path.append("../scikit/")
import cleaner
import os
from nltk.stem.snowball import EnglishStemmer
import nltk
import multiprocessing as mp
import time
from sklearn.feature_extraction.text import TfidfVectorizer

#### inputs
HOST = ''
PORT = 8002

#### service paths
classifier_path="./"

#### key parameters
language='en'
path2save="./data/"

sumc=200


'''
Main class for connecting
'''
class connection:


    def __init__(self):

        global conn
        global server
        global clean
        global proclist
        clean=cleaning_data()

        server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        print 'INFO -> Socket created'

        try:
            server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR,1)
            server.bind((HOST, PORT))
        except socket.error as msg:
            print 'ERROR -> Bind failed. Error Code : ' + str(msg[0]) + ' Message ' + msg[1]
            sys.exit()

        print 'INFO -> Socket bind complete'

        server.listen(1024)
        print 'INFO -> Socket now listening'
        keywords=self.loadkeywords()
        #now keep talking with the client
        #while 1:
        proclist=[]
        if 1==1:
            #wait to accept a connection - blocking call

            conn, addr = server.accept()
            print 'INFO -> Connected with ' + addr[0] + ':' + str(addr[1])
            conn.send("INFO -> The available keywords are :"+str(keywords)+"\n")
            while True:
                data = conn.recv(1024)
                if not data: break
                if data:
                    self.mainstep(data,keywords)



        server.close()




        conn.send("INFO -> Finished : "+ str(z)+" "+str(len(proclist))+"\n")

    def mainstep(self,data,keywords):
        r4d,accept=self.checkdata(data)
        if accept==True:
                    inputd=r4d['categorise']
                    ok=[]
                    texts=[]
                    links=[]
                    ids=[]
                    urls=[]
                    rawl=[]
                    ind=inputd[0]
                    id=ind.split(",")[0]
                    url=ind.split(",")[1]
                    link=urllib2.quote(url)



                    ####
                    #
                    ####  Not finished ... quick work around .... but NOT error prone
                    ##
                    ####
                    if (url.split(".")[0].isdigit()) and ("http://" not in url):
                        link="http://"+link

                    elif ("www." not in url) and (url.split(".")[0].isdigit()==False) and ("http://" not in url):
                        link="http://www."+link

                    elif ("http://" not in url) and (url.split(".")[0].isdigit()==False):
                        print link
                        link="http://"+link


                    link=link.replace("%3A",":").replace("%C2%A0%C2%A0%C2%A0%C2%A0","").replace("%5EM","")
                    raw,text,accept=clean.stripping(link)
                    ok.append(accept)
                    texts.append(text)
                    ids.append(id)
                    urls.append(link)
                    rawl.append(raw)
                    if accept==True:
                        name=link.strip("://.").replace("http://","").replace("/","")


                        blogs,per=cleaner.blogcheck(raw,url)
                        if blogs:
                            result=id+",blog,"+str(per)
                            result2="blog,"+str(per)
                            conn.send(result+' \n')
                        else:
                            result,result2=self.categorise_classifier(urls,texts,ids,ok,keywords,rawl)

                        name=name+"_"+result2.replace(",","_")
                        if os.path.exists(path2save+"/"+name)==False:
                            tfile=open(path2save+name,"w")
                            print >> tfile,raw
                            tfile.close()

    def checkdata(self,data):

        r4d={}
        accept=False

        xlist=data.split()
        if len(xlist)>0:
            #service=xlist[0]
            accept=False
            d4iu=xlist[0].split(",")

            if len(d4iu)==2: # quick work around to make it running
                #conn.send('INFO -> Thank you data received and in correct format\n')
                #conn.send('INFO -> Following links will be classified'+str(url)+"\n")
                r4d["categorise"]=xlist
                accept=True
            else:
                conn.send('ERROR -> Data not in correct format data1.0,data1.1  \n')
                conn.send('INFO -> For example for categorise use "id1,url1" \n')
                #time.sleep(2)
                #conn.send('ERROR -> Refusing connection..bye \n')
        else:
            conn.send('ERROR -> No service specified! \n')

        return r4d,accept

    def categorise_classifier(self,urls,texts,ids,ok,keywords,rawl):

        texts = vectorizer.transform(texts)

        prediction=clf.predict(texts)

        prediction_pro=clf.predict_proba(texts)

        for i in range(len(urls)):

            if ok[i]==True:
                keywords=sorted(keywords)
                prob=prediction_pro[i][keywords.index(prediction[i])]
                result=ids[i]+","+prediction[i]+","+str(prob)
                result2=prediction[i]+","+str(prob)
                conn.send(result+"\n")
                self.writedata(urls[i],prediction[i],prob,rawl[i])
            else:
                result=ids[i]+",ERROR,NULL"
                result2="ERROR,NULL"
                conn.send(result+"\n")

        return result,result2

    def loadkeywords(self):

        lines=open("./keywords.txt","r").readlines()
        keywords=[]
        for line in lines:
            keywords.append(line.split()[0])

        print "INFO -> keywords",keywords

        return keywords

    def writedata(self,url,prediction,prob,raw):

        name=prediction+"_"+url.strip("://.").replace("http://","").replace("/","")+"_"+str(prob)+".txt"

        files=os.listdir(path2save)

        for file in files:

            if name not in file:

                file=open(path2save+"/"+name,"w")
                print >> file,raw

                file.close()




'''
class for cleaning the data for the categoriser
'''
class cleaning_data:

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

    def gettinglan(self,text):

        lanre=langid.classify(text)
        language= lanre[0]
        certainty=lanre[1]

        return language,certainty



    def stripping(self,url):


         raw,text,accept=cleaner.getdata(language,sumc,url)

         return raw,text,accept


####################
# Main
####################
stemmer = EnglishStemmer()

def stem_tokens(tokens, stemmer):
    stemmed = []
    for item in tokens:
        stemmed.append(stemmer.stem(item))
    return stemmed


def tokenize(text):
    tokens = nltk.word_tokenize(text)
    stems = stem_tokens(tokens, stemmer)

    #print len(stems)
    #stems=removecommonwords(stems)
    #print len(stems)
    #print "*************************"

    return stems

print 'loading data...'
clf = joblib.load(classifier_path+"/categoriser_NB_11022015.pkl")
print 'done...'
print 'loading vectors...'
vectorizer = TfidfVectorizer(tokenizer=tokenize,strip_accents='unicode',ngram_range=(1, 5),stop_words='english',norm='l2')
print 'done...'
print 'loading classifier/trained data...'
x_train = joblib.load(classifier_path+"/x_train_NB_11022015.pkl")
print 'done...'
print 'loading transform...'
vectorizer.fit_transform(x_train)
print 'done...'

#print 'dumping...'
#joblib.dump(vectorizer, 'vectorizer.pkl', compress=9)
#print 'done'


connection()
