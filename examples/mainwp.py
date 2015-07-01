__author__ = 'gvanbavi'

## imports
from sklearn import datasets
import downloaderwp
import os
import sys
import multiprocessing as mp
import analyser
import time
import psutil
import numpy
import random

## switches:
cvsLinkDataAvailable=True
trainingDataHere=False
analysingData=True
an = analyser.analyser()

##
ntest=100
ntrainer=4000
takeFullTrainerData=True


keywords=["Blog","Directory","Government"]
#keywords=["Government"]
#keywords=["Test"]
language=["en"]
path="./data/cvs/"
path2save="./data/category/"
path2classifier="./data/classifiers/"

## mainwp class
class mainwp(object):
    def __init__(self):
        print "INFO ->> Main class initialised"

        if cvsLinkDataAvailable==False:
            print "INFO ->> Getting cvs link data"
            self.getdmozlinks()

        if trainingDataHere==False:
            print "INFO ->> Downloading training data"
            self.gettrainingdata()

        if analysingData==False:
            print "INFO ->> Analysing data"
            trainerData=self.analysingData()
            testdata,trainerData,outcome=self.extractTestdata(trainerData,ntrainer)
            classifier=an.trainer(trainerData,path2classifier,ntrainer,keywords)
            result=an.classify(classifier,testdata)
            print "INFO ->> How exact are the results",self.comparer(result,outcome)
            print "INFO ->> Training data",len(trainerData)
            print "INFO ->> Test data",len(testdata)

    def gettrainingdata(self):
        down=downloaderwp.downloader()
        if os.path.exists(path):
            cvsfiles=os.listdir(path)
            cvslist=[]
            cvstext={}

            #loading cvs text files
            for cvsfile in cvsfiles:
                keywordfromfile=cvsfile.split("_")[1].split(".")[0]
                cvslist.append(keywordfromfile)
                data=open(path+"/"+cvsfile,"r").readlines()
                cvstext[keywordfromfile]=data

            cvstexta={}
            for keyword in keywords:
                    if keyword not in cvslist:
                        print cvsfile.split("_")[1].split(".")
                        print "ERROR ->> cvsfile for keyword",keyword,"does not exist \n you should run with cvsLinkDataAvailable=False"
                        sys.exit(1)
                    else:
                        cvstexta[keyword]=cvstext[keyword]


            for key in cvstexta:
                lines=cvstexta[key]
                print "INFO ->> Will download a total of ",len(lines)," for keyword",key

                processes=[]
                for line in lines:

                    processes.append(mp.Process(target=down.preparelines,args=(line,key,path2save)))

                subp=[]
                for p in processes:
                    self.cpuchecker()
                    p.start()

                    subp.append(p)
                    subp=self.breakcheck(subp)
                    while(len(subp)>16):
                        subp=self.breakcheck(subp)


                '''
                for p in processes:
                    self.cpuchecker()
                    p.join()
                '''
        else:
            print "ERROR ->> Path",path," doesn't exit"
            sys.exit(1)

    def breakcheck(self,subp):
        temp=[]
        for sp in subp:
            if sp.is_alive():
                temp.append(sp)


        subp=temp

        return subp


    def cpuchecker(self):
        psus=psutil.cpu_percent(interval=0.0, percpu=True)
        while(numpy.average(psus)>80):
            #print "WARNING ->> CPU overload, going to bed for 5 seconds",psus
            psus=psutil.cpu_percent(interval=0.0, percpu=True)
            time.sleep(1)

    def analysingData(self):
        trainerData=[]
        for keyword in keywords:
            files=os.listdir(path2save+"/"+keyword+"/")
            processes=[]
            count=0
            for file in files:
                if (os.path.isfile(path2save+"/"+keyword+"/"+file)) and (count<ntrainer):

                    #processes.append(mp.Process(target=an.cleaninghtml,args=(path2save+"/"+keyword+"/",file,language,trainerData,keyword)))
                    correct,passs=an.cleaninghtml(path2save+"/"+keyword+"/",file,language,keyword)
                    if passs:
                        count=count+1
                        trainerData.append(correct)
            print len(trainerData),keyword
        '''
        subp=[]
        print "INFO ->> Going to analyse for",len(processes)
        for p in processes:
            self.cpuchecker()
            p.start()

            subp.append(p)
            subp=self.breakcheck(subp)
            while(len(subp)>16):
                subp=self.breakcheck(subp)

        '''

        return trainerData

    def extractTestdata(self,trainerData,ntrainer):
        random.shuffle(trainerData)

        testdata=[]
        outcome=[]
        print len(trainerData)
        for i in range(ntest):
            value=random.choice(trainerData)
            testdata.append(value[0])
            outcome.append(value[1])
            trainerData.remove(value)

        if  takeFullTrainerData==True:
            ntrainer=len(trainerData)

        trainerData=trainerData[:ntrainer]

        print "INFO ->> Length of training data",len(trainerData),"and length of test data",len(testdata)

        return testdata,trainerData,outcome

    def comparer(self,result,outcome):
        match=0

        if len(result)!=len(outcome):
            print "ERROR ->> FATAL error outcome list does not agree in length"
            sys.exit(1)

        for i in range(len(result)):
            if result[i]==outcome[i]:
                match=match+1

        return match/(len(result)*1.0)



print "INFO ->> Starting task with ", keywords
#mainwp()

