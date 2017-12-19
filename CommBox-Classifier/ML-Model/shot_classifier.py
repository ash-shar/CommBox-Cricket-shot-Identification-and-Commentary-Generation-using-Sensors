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


basepath = os.path.dirname(os.path.abspath(__file__))+"/Train/"

model_path = os.path.dirname(os.path.abspath(__file__))+"/TrainedClassifiers_new"
# test_path = os.path.dirname(os.path.abspath(__file__))+"/Test/test_vectors.txt"
eval_path = os.path.dirname(os.path.abspath(__file__))+"/Evaluation"

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

ignore_path = "wrong.txt"


names = ["NN", "SVM", "DT",
        "NB" ]

classifiers = [
    KNeighborsClassifier(3),
    SVC(kernel="linear", C=0.025),
    DecisionTreeClassifier(max_depth=6),
    GaussianNB()]


evaluation_names = ["Accuracy"]

def evaluate(y_true,y_pred):
    # print(y_true)
    # print(y_pred)

    return [accuracy_score(y_true, y_pred)]
    # f1_score(y_true, y_pred, average='micro'),
    # precision_score(y_true, y_pred, average='micro')

err_cnt = 0

l = [1,2,3,4]
print(l[:-1])

def reduce_dimension(orig,remove_last=False):
    global err_cnt
    # orig = get_sensor_data(filename)
    n = len(orig)
    m  = n // finaldim

    # print("m: ",m)



    if(m == 0):
        print("Error 1")
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

    # print(flat_vec)

    return flat_vec



def load_train_dataset(train_path):
    folders = os.listdir(train_path)

    X_train = []
    y_train = []

    for foldername in folders:
        files = os.listdir(train_path+'/'+foldername)

        ignore_file = codecs.open(train_path+'/'+foldername+'/'+ignore_path,'r','utf-8')
        ignore = []
        for row in ignore_file:
            s = row.strip().split(',')
            for elem in s:
                ignore.append(int(elem.strip())-1)
        print(foldername," ign ", ignore, len(ignore))

        last_ign = ignore[-1]

        
        # print(foldername)
        if foldername == ".DS_Store":
            continue

        pre_file = codecs.open(train_path+"/"+foldername+'/'+comp_pre_path,'r','utf-8')

        post_file = codecs.open(train_path+"/"+foldername+'/'+comp_post_path,'r','utf-8')

        # pre_text = pre_file.read().split('\n')

        post_text = post_file.read().split('\n')


        pre_file_1 = codecs.open(train_path+"/"+foldername+'/'+gyro_pre_path,'r','utf-8')
        post_file_1 = codecs.open(train_path+"/"+foldername+'/'+gyro_post_path,'r','utf-8')


        pre_text_1 = pre_file_1.read().split('\n') 
        post_text_1 = post_file_1.read().split('\n')


        pre_file_2 = codecs.open(train_path+"/"+foldername+'/'+acc_pre_path,'r','utf-8')
        post_file_2 = codecs.open(train_path+"/"+foldername+'/'+acc_post_path,'r','utf-8')


        pre_text_2 = pre_file_2.read().split('\n') 
        post_text_2 = post_file_2.read().split('\n')

        ii = 0
        # print("here =")

        for row in pre_file:
            # print("in")
            if ii in ignore:
                ii+=1
                continue
            
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

            # l_new = eval(sc[ii])

            # newvec_new = reduce_dimension(l_new[to_remove:-to_remove])

            # if newvec_new == 0:
            #     ii = ii+1
            #     continue

            # l_acc = eval(sacc[ii])

            # newvec_acc= reduce_dimension(l_acc[to_remove:-to_remove])

            # if newvec_acc == 0:
            #     ii = ii+1
            #     continue

            ii = ii+1

            # final_vec = pre_vec+post_vec+pre_vec_1+post_vec_1+pre_vec_2+post_vec_2
            
            final_vec = pre_vec_1+post_vec_1+pre_vec_2+post_vec_2
            # print(final_vec)
            X_train.append(final_vec)

            y_train.append(foldername)

        print("fold: ",foldername)

    print(len(X_train),len(y_train))

    # print(X_train,y_train)

    return X_train,y_train


def train_model():

    print("trainig started")
    X_train,y_train = load_train_dataset(basepath)

    for algo, clf in zip(names, classifiers):
        clf.fit(X_train, y_train)
        with open(model_path+"/"+algo + '.pkl', 'wb') as f1:
            pickle.dump(clf, f1)
        print("Trained with "+algo)
    


def test_model(algo):
    try:
        with open(model_path+"/"+algo + '.pkl', 'rb') as f1:
            clf = pickle.load(f1)

    except:
        print(algo+" Classifier not trained yet...Please train it and try again")

    file = codecs.open(test_path)

    for row in file:
        l = eval(row.strip())
        newvec = reduce_dimension(l[to_remove:-to_remove])
        if newvec == 0:
            continue
        vector = np.matrix(newvec)
        # predicted+=[clf.predict(vector)[0]]

        print(clf.predict(vector)[0])


def train_model_k_fold(X_train,y_train):

    print("trainig started")
    # X_train,y_train = load_train_dataset(basepath)

    for algo, clf in zip(names, classifiers):
        clf.fit(X_train, y_train)
        # with open(model_path+"/"+algo + '.pkl', 'wb') as f1:
        #     pickle.dump(clf, f1)
        # print("Trained with "+algo)




def k_fold_validation_old():

    X,y = load_train_dataset(basepath)

    X = np.array(X)
    y = np.array(y)

    for algo, clf in zip(names, classifiers):

        scores = []

        kf = KFold(n = len(X),n_folds=10)

        for train_index, test_index in kf:
            # print("TRAIN:", train_index, "TEST:", test_index)
            X_train, X_test = X[train_index], X[test_index]
            y_train, y_test = y[train_index], y[test_index]

            clf.fit(X_train, y_train)

            predicted = []

            for ind in range(0,len(X_test)):
                vector = np.matrix(X_test[ind])
                predicted+=[clf.predict(vector)[0]]

            scores.append(evaluate(y_test,np.array(predicted)))

        eval_methods = {}
        for score in scores:
            ii= 0
            for eval_method in score:
                # print(algo,evaluation_names[ii],eval_method)
                if evaluation_names[ii] in eval_methods.keys():
                    eval_methods[evaluation_names[ii]].append(eval_method)
                else:
                    eval_methods[evaluation_names[ii]] = []
                    eval_methods[evaluation_names[ii]].append(eval_method)
                ii+=1

        average_scores = {}

        for key in eval_methods:
            li = eval_methods[key]
            sum = 0
            for item in li:
                sum = sum+item
            sum /= len(li)

            average_scores[key] = sum

        for key in average_scores:
            print(algo,key, average_scores[key])



        # print(algo, scores)




def k_fold_validation():

    X,y = load_train_dataset(basepath)

    X = np.array(X)
    y = np.array(y)

    for algo, clf in zip(names, classifiers):

        scores = []

        clf.fit(X,y)

        scores = cross_validation.cross_val_score(clf,X,y,cv=10)
        print(algo,scores.mean())



        # print(algo, scores)




def main():
    global err_cnt
    # typ = sys.argv[1]

    train_model()

    # test_model("SVM")

    # k_fold_validation()

    print(err_cnt)


    # if typ == "1":7]
    #     train_model()

    # elif typ == "2":
    #     algo = sys.argv[2]
    #     test_model(algo)

    # else:
    #     print("Invalid input")

    

    # files = os.listdir(basepath)

    # for filename in files:
    #     if filename == ".DS_Store":
    #         continue

    #     train_path = basepath+"/"+dir+"/"+sub_dir+"/Train"
    #     test_path = basepath+"/"+dir+"/"+sub_dir+"/Test"

    #     write_file = codecs.open(output_path+"/"+dir+"_"+sub_dir+"-output.txt",'w','utf-8')
    #     eval_file = codecs.open(eval_path+"/"+dir+"_"+sub_dir+"-evaluation_scores.txt",'w','utf-8')

    #     X_train,y_train = load_train_dataset(train_path)

    #     X_test,y_true = load_test_dataset(test_path)

    #     print(train_path,test_path)

    #     for algo, clf in zip(names, classifiers):
    #         try:
    #             with open(model_path+"/"+dir+"/"+sub_dir+"/"+algo + '.pkl', 'rb') as f1:
    #                 clf = pickle.load(f1)
    #         except:
    #             clf.fit(X_train, y_train)
    #             with open(model_path+"/"+dir+"/"+sub_dir+"/"+algo + '.pkl', 'wb') as f1:
    #                 pickle.dump(clf, f1)

    #         predicted = []
    #         print(algo+"_fitted")

    #         for ind in range(0,len(X_test)):
    #             vector = np.matrix(X_test[ind])
    #             predicted+=[clf.predict(vector)[0]]
            
    #         print(algo, predicted, file=write_file)
    #         print(algo+"_Tested")

    #         scores = evaluate(y_true,predicted)
    #         print(algo+"\t"+str(scores),file=eval_file)
    #     print(filename)


if __name__ == "__main__":main()
