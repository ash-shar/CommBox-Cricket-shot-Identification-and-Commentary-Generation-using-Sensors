import codecs
import numpy as np
from sklearn.decomposition import PCA

def get_sensor_data(filename):
	l = list()
	with codecs.open(filename, 'r', encoding='utf-8') as f:
		for line in f:
			l.append(float(line))
	return l

def reduce_dimension(filename, finaldim):
	orig = get_sensor_data(filename)
	n = len(orig)
	m  = n / finaldim
	newvec = list()
	for i in range(n):
		if i % m == 0:
			newvec.append(orig[i])
	return newvec

def reduce_dimension_pca(filename, finaldim):
	with codecs.open(filename) as f:
		for row in f:
			orig = eval(row.strip())
			pca = PCA(n_components=finaldim)
			orig = np.array(orig)
			print(orig)
			pca.fit(orig)
			newvec = pca.explained_variance_ratio_
		return newvec

if __name__ == '__main__':
	newvec = reduce_dimension('gyro.txt', 6)
	print(newvec)
	reduce_dimension_pca('gyro.txt', 6)
	print(newvec)