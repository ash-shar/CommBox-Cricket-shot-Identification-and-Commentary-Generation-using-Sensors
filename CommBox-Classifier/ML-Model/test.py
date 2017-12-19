from sklearn.cross_validation import train_test_split
from sklearn.preprocessing import StandardScaler
from sklearn.datasets import make_moons, make_circles, make_classification
from sklearn.neighbors import KNeighborsClassifier
from sklearn.svm import SVC
from sklearn.tree import DecisionTreeClassifier
from sklearn.ensemble import RandomForestClassifier, AdaBoostClassifier
from sklearn.naive_bayes import GaussianNB
from sklearn.discriminant_analysis import LinearDiscriminantAnalysis
from sklearn.discriminant_analysis import QuadraticDiscriminantAnalysis

from sklearn import cross_validation

from sklearn.cross_validation import KFold

from sklearn.metrics import accuracy_score
from sklearn.metrics import average_precision_score
from sklearn.metrics import f1_score
from sklearn.metrics import log_loss
from sklearn.metrics import precision_score
from sklearn.metrics import recall_score
from sklearn.metrics import roc_auc_score

from sklearn.decomposition import PCA

import os
import codecs
import sys
import numpy as np
import pickle


basepath = os.path.dirname(os.path.abspath(__file__))+"/Training_Data/sumit/"

model_path = os.path.dirname(os.path.abspath(__file__))+"/TrainedClassifiers_new"
test_path = os.path.dirname(os.path.abspath(__file__))+"/Test"
eval_path = os.path.dirname(os.path.abspath(__file__))+"/Evaluation"
result_path = os.path.dirname(os.path.abspath(__file__))+"/Result/result.txt"

finaldim = 30

num = 65

to_remove = 0


rot_pre_path = "test_vectors_rot_pre.txt"
rot_post_path = "test_vectors_rot_post.txt"

gyro_pre_path = "test_vectors_gyro_pre.txt"
gyro_post_path = "test_vectors_gyro_post.txt"

comp_pre_path = "test_vectors_comp_pre.txt"
comp_post_path = "test_vectors_comp_post.txt"

acc_post_path = "test_vectors_acc_post.txt"
acc_pre_path = "test_vectors_acc_pre.txt"


def reduce_dimension(orig,remove_last=False):
    global err_cnt
    # orig = get_sensor_data(filename)
    n = len(orig)
    m  = n // finaldim

    # print("m: ",m)



    if(m == 0):
        print("Error")
        err_cnt += 1
        return 0


    # print(n,m,finaldim)

    # print(m)
    newvec = list()
    for i in range(n):
        if i % m == 0 and len(newvec) < finaldim:
            if remove_last:
                newvec.append(orig[i][:-1])
            else:
                newvec.append(orig[i])
    flat_vec = []

    # print(newvec)

    for elem in newvec:
        for inner_elem in elem:
            flat_vec.append(inner_elem)

    # print(len(flat_vec))

    return flat_vec

def test_model(algo):
    try:
        with open(model_path+"/"+algo + '.pkl', 'rb') as f1:
            clf = pickle.load(f1)

    except:
        print(algo+" Classifier not trained yet...Please train it and try again")

    pre_file = codecs.open(test_path+'/'+comp_pre_path,'r','utf-8')
    post_file = codecs.open(test_path+'/'+comp_post_path,'r','utf-8')

    pre_file_1 = codecs.open(test_path+'/'+gyro_pre_path,'r','utf-8')
    post_file_1 = codecs.open(test_path+'/'+gyro_post_path,'r','utf-8')


    pre_file_2 = codecs.open(test_path+'/'+acc_pre_path,'r','utf-8')
    post_file_2 = codecs.open(test_path+'/'+acc_post_path,'r','utf-8')


    post_text = post_file.read().split('\n')

    pre_text_1 = pre_file_1.read().split('\n') 
    post_text_1 = post_file_1.read().split('\n')

    pre_text_2 = pre_file_2.read().split('\n') 
    post_text_2 = post_file_2.read().split('\n')

    ii = 0

    for row in pre_file:

        l = eval(row.strip())
        l = l[70-num:]
        pre_vec = reduce_dimension(l)
        if pre_vec == 0:
            ii = ii+1
            continue


        l_post = eval(post_text[ii].strip())
        l_post = l_post[:num]
        post_vec = reduce_dimension(l_post)

        if post_vec == 0:
            ii = ii+1
            continue


        l_pre_1 = eval(pre_text_1[ii].strip())
        l_pre_1 = l_pre_1[70-num:]
        # print("pre ",len(l_pre_1))
        pre_vec_1 = reduce_dimension(l_pre_1, remove_last = True)

        if pre_vec_1 == 0:
            ii = ii+1
            continue


        l_post_1 = eval(post_text_1[ii].strip())
        l_post_1 = l_post_1[:num]
        # print("post ",len(l_post_1))
        post_vec_1 = reduce_dimension(l_post_1, remove_last = True)

        if post_vec_1 == 0:
            ii = ii+1
            continue


        l_pre_2 = eval(pre_text_2[ii].strip())
        l_pre_2 = l_pre_2[70-num:]
        # print("pre ",len(l_pre_1))
        pre_vec_2 = reduce_dimension(l_pre_2, remove_last = True)

        if pre_vec_2 == 0:
            ii = ii+1
            continue


        l_post_2 = eval(post_text_2[ii].strip())
        l_post_2 = l_post_2[:num]
        # print("post ",len(l_post_1))
        post_vec_2 = reduce_dimension(l_post_2, remove_last = True)

        if post_vec_2 == 0:
            ii = ii+1
            continue


        # newvec = pre_vec+post_vec+pre_vec_1+post_vec_1+pre_vec_2+post_vec_2
            
        newvec = pre_vec_1+post_vec_1+pre_vec_2+post_vec_2
        vector = np.matrix(newvec)
		# predicted+=[clf.predict(vector)[0]]

        predicted = clf.predict(vector)[0]

        print(predicted)

        result = open(result_path, 'w')
        result.write("cut")


test_model("NN-2")


