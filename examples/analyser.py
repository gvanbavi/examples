__author__ = 'gvanbavi'

import bs4
from collections import Counter
import langid
import numpy
import nltk
import pickle
import urllib2
import random

## analyser class
class analyser(object):
    def __init__(self):
        print "INFO ->> Analyser class initialised"

    def cleaninghtml(self,path,file,language,keyword):

        html=open(path+"/"+file,"r")
        raw=bs4.BeautifulSoup(html)
        links=raw.find_all('a')
        data=raw.find_all(text=True)
        passs=False
        correct=()

        [s.extract() for s in raw(['style', 'script', '[document]', 'head', 'title'])]
        text=raw.get_text().encode('utf-8')

        if (self.gettinglan(text)[0] in language) and (self.gettinglan(text)[1]>0.99):

            counter=Counter(text.lower().strip(".:;?!,").replace("\n","").replace(" ",""))
            values=sorted(counter.iteritems())
            suml=[]
            for value in values:
                suml.append(value[1])

            sum=numpy.sum(suml)
            if sum>200:
                correct=({"text":text},keyword)
                passs=True
            else:
                print "WARNING -> skipping website, not enough characters",sum

        return correct,passs

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

    def cleaninghtmlc(self,website):

        req = urllib2.Request(website, None, {'User-agent': self.agents()})
        response=urllib2.urlopen(website)
        html= response.read()

        raw=bs4.BeautifulSoup(html)
        links=raw.find_all('a')
        data=raw.find_all(text=True)
        passs=False
        correct=()

        [s.extract() for s in raw(['style', 'script', '[document]', 'head', 'title'])]
        text=raw.get_text().encode('utf-8')

        correct=({"text":text})

        return correct

    def gettinglan(self,text):

        lanre=langid.classify(text)
        language= lanre[0]
        certainty=lanre[1]

        return language,certainty

    def trainer(self,trainerData,path2classifier,ntrain,keywords):

        print "INFO ->> Starting to train"

        classifier=nltk.classify.DecisionTreeClassifier.train(trainerData)

        name="_".join(keywords)+"_"+str(ntrain)

        f = open(path2classifier+'classifier_'+name+'.pickle', 'wb')
        pickle.dump(classifier, f)
        f.close()

        print "INFO ->> Finished training"

        return classifier

    def classify(self,classifier,classifyData):

        result=classifier.classify_many(classifyData)

        return result
