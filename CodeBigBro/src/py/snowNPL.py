#coding=gbk
'''
Created on 2015��9��24��

@author: Tassadar
'''

# Requirement:unicode
from snownlp import SnowNLP
import sys

a=sys.argv[1]
s=u'����һ���ǳ������test������'
r=SnowNLP(s)

f=open("F:\\testOutput.txt","a")
print(r.keywords(a, False),file=f)

f.close()






