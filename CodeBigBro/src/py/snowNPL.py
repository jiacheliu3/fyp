#coding=gbk
'''
Created on 2015年9月24日

@author: Tassadar
'''

# Requirement:unicode
from snownlp import SnowNLP
import sys

a=sys.argv[1]
s=u'这是一个非常随意的test天了噜'
r=SnowNLP(s)

f=open("F:\\testOutput.txt","a")
print(r.keywords(a, False),file=f)

f.close()






