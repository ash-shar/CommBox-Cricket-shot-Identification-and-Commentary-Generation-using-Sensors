import os
import codecs
import plotly
import plotly.graph_objs as go


N = 25
THRESHOLD = 2


def reduceDimension(orig):
    n = len(orig)
    m  = n // N
    if(m == 0):
        return 0
    newvec = list()
    for i in range(n):
        if i % m == 0 and len(newvec) < N:
            newvec.append(orig[i])
    return newvec


def getDimension(dim, basepath, sensor):
    files = os.listdir(basepath+sensor+"/")
    plot_dict = dict()
    for filename in files:
        if filename == ".DS_Store":
            continue
        plot_dict[filename] = list()
        file = codecs.open(basepath+sensor+"/"+filename,'r','utf-8')
        count = 0
        for row in file:
            sample = list()
            sample = eval(row.strip())
            sample = reduceDimension(sample)
            vec = list()
            for s in sample:
                vec.append(s[dim])
            plot_dict[filename].append(vec)
            count += 1
            if count == THRESHOLD:
                break
    return plot_dict


def plot_on_dimension(sensor, dim):
    basepath = os.path.dirname(os.path.abspath(__file__))+"/../featureExtraction/"
    for i in range(N):
        X = i+1
    plot_dict = getDimension(dim, basepath, sensor)
    trace0 = list()
    trace1 = list()
    trace2 = list()
    for i in range(THRESHOLD):
        trace0.append(go.Scatter(
            x = X,
            y = plot_dict['pull-'+sensor+'.txt'][i],
            name = 'PULL',
            line = dict(color = ('rgb(244, 66, 66)'))
        ))
        trace1.append(go.Scatter(
            x = X,
            y = plot_dict['straight-'+sensor+'.txt'][i],
            name = 'STRAIGHT',
            line = dict(color = ('rgb(66, 244, 113)'))
        ))
        trace2.append(go.Scatter(
            x = X,
            y = plot_dict['cut-'+sensor+'.txt'][i],
            name = 'CUT',
            line = dict(color = ('rgb(78, 66, 244)'))
        ))
    data = trace0
    data += trace1
    data += trace2
    if dim == 0:
        ch = 'x'
    elif dim == 1:
        ch = 'y'
    elif dim == 2:
        ch = 'z'
    elif dim == 3:
        ch = 'rad'
    plotly.offline.plot(data, filename=sensor+'-'+ch+'.html')


def main():
    plot_on_dimension('gyro', 0)
    plot_on_dimension('gyro', 1)
    plot_on_dimension('gyro', 2)

    plot_on_dimension('acc', 0)
    plot_on_dimension('acc', 1)
    plot_on_dimension('acc', 2)
    
    plot_on_dimension('rot', 0)
    plot_on_dimension('rot', 1)
    plot_on_dimension('rot', 2)
    plot_on_dimension('rot', 3)


if __name__ == '__main__':
    main()
