import pandas
from sklearn.neighbors import KNeighborsClassifier

parks = dict()
weathers = dict()
dirs = dict()


#################################
# Convert non numerical columns #
#################################
def encode_columns(dataset):
    dataset["car_park_name"] = dataset["car_park_name"].map(parks)
    dataset["weather"] = dataset["weather"].map(weathers)
    dataset["direction"] = dataset["direction"].map(dirs)
    return dataset


def my_main():
    training_dataset = pandas.read_csv("machine_learning/data.csv", encoding="ISO-8859-1", low_memory=False)
    test = pandas.read_csv("machine_learning/today.csv", encoding="ISO-8859-1", low_memory=False)

    global parks
    global weathers
    global dirs
    i1 = 0
    i2 = 0
    i3 = 0
    for each in training_dataset["car_park_name"]:
        if each not in parks:
            parks[each] = i1
            i1 += 1
    for each in training_dataset["weather"]:
        if each not in weathers:
            weathers[each] = i2
            i2 += 1
    for each in training_dataset["direction"]:
        if each not in dirs:
            dirs[each] = i3
            i3 += 1

    training_dataset = encode_columns(training_dataset)
    test = encode_columns(test)

    training_dataset["time"] = training_dataset["time"] - training_dataset["time"].min()

    times = training_dataset["time"]
    training_dataset = training_dataset.drop(["time"], axis=1)
    nearest_n = KNeighborsClassifier()
    nearest_n.fit(training_dataset, times)
    result = nearest_n.predict(test)
    final_result = pandas.DataFrame(result)
    test["time"] = final_result
    parks = dict((v, k) for k, v in parks.items())
    weathers = dict((v, k) for k, v in weathers.items())
    dirs = dict((v, k) for k, v in dirs.items())
    encode_columns(test)
    test.to_csv("machine_learning/result.csv", index=False, header=True)


if __name__ == '__main__':
    my_main()
