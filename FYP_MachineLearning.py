import pandas
from sklearn.neighbors import KNeighborsClassifier
from sklearn.preprocessing import LabelEncoder

directions = set()
car_parks = set()


#################################
# Convert non numerical columns #
#################################
def encode_columns(dataset):
    labelencoder = LabelEncoder()
    dataset["car_park_name"] = labelencoder.fit_transform(dataset["car_park_name"])
    dataset["weather"] = labelencoder.fit_transform(dataset["weather"])
    dataset["direction"] = labelencoder.fit_transform(dataset["direction"])
    return dataset


def get_delay(dataset):
    global directions
    global car_parks
    for each in dataset["direction"]:
        directions.add(each)
    for each in dataset["car_park_name"]:
        car_parks.add(each)


def my_main():
    train = pandas.read_csv("machine_learning/data.csv", encoding="ISO-8859-1", low_memory=False)
    test = pandas.read_csv("machine_learning/today.csv", encoding="ISO-8859-1", low_memory=False)

    get_delay(train)
    
    train = encode_columns(train)
    test = encode_columns(test)

    label_train = train["time"]
    train = train.drop(["time"], axis=1)
    nearest_n = KNeighborsClassifier()
    nearest_n.fit(train, label_train)
    result = nearest_n.predict(test)
    final_result = pandas.DataFrame(result)
    test["time"] = final_result
    test.to_csv("machine_learning/result.csv", index=False, header=True)


if __name__ == '__main__':
    my_main()
