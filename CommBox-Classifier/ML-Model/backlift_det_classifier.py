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

basepath = os.path.dirname(os.path.abspath(__file__))+"/../featureExtraction-7/gyro/"
basepath_rot = os.path.dirname(os.path.abspath(__file__))+"/../featureExtraction-7/rot/"

basepath_acc = os.path.dirname(os.path.abspath(__file__))+"/../featureExtraction-7/acc/"

model_path = os.path.dirname(os.path.abspath(__file__))+"/TrainedClassifiers"
test_path = os.path.dirname(os.path.abspath(__file__))+"/Test/test_vectors.txt"
eval_path = os.path.dirname(os.path.abspath(__file__))+"/Evaluation"

finaldim = 25

to_remove = 30


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

def reduce_dimension(orig):
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
            newvec.append(orig[i])
    flat_vec = []

    # print(newvec)

    for elem in newvec:
        for inner_elem in elem:
            flat_vec.append(inner_elem)

    # print(len(flat_vec))

    return flat_vec



def load_train_dataset(train_path):
    files = os.listdir(train_path)

    X_train = []
    y_train = []

    for filename in files:
        print(filename)
        if filename == ".DS_Store":
            continue
        file = codecs.open(train_path+"/"+filename,'r','utf-8')

        rot_filename = filename.split('-')[0]+'-rot.txt'

        acc_filename = filename.split('-')[0]+'-acc.txt'

        file_rot = codecs.open(basepath_rot+'/'+rot_filename,'r','utf-8')

        file_acc = codecs.open(basepath_acc+'/'+acc_filename,'r','utf-8')

        rot_txt = file_rot.read()
        sc = rot_txt.strip().split('\n')

        acc_txt = file_acc.read()
        sacc = acc_txt.strip().split('\n')

        ii = 0

        for row in file:
            l = eval(row.strip())
            newvec = reduce_dimension(l[to_remove:-to_remove])
            if newvec == 0:
                ii = ii+1
                continue

            l_new = eval(sc[ii])

            newvec_new = reduce_dimension(l_new[to_remove:-to_remove])

            if newvec_new == 0:
                ii = ii+1
                continue

            # l_acc = eval(sacc[ii])

            # newvec_acc= reduce_dimension(l_acc[to_remove:-to_remove])

            # if newvec_acc == 0:
            #     ii = ii+1
            #     continue

            ii = ii+1

            final_vec = newvec+newvec_new
            X_train.append(final_vec)

            y_train.append(filename.split('.')[0])

        print(filename)

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




def k_fold_validation():

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









def main():
    global err_cnt
    # typ = sys.argv[1]

    train_model()

    test_model("SVM")

    # k_fold_validation()

    print(err_cnt)

if __name__ == "__main__":main()
