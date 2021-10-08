from numpy import mod, real
import pandas as pd
from pandas._config import config

model_names = [#"MM1LowU", "MM1HighU", "MM2HighU", "3Series", "ParallelQueues", "ParallelErlang", "3Closed", "3Serieswithrework","2CSS","2CDS",
            #"3Serieswithreworkclass",   "3Serieswith2reworkclasses",  "ParallelServers", 
             "Burst",#"MM110",  "2CDSDC",
                #"ParallelService"
                ]

class_models = {
    "one_queue": ["MM1LowU", "MM1HighU", "MM2HighU", "2CSS", "2CDS", "MM110", "2CDSDC"],
    "cyclical": ["3Closed", "3Serieswithrework", "3Serieswithreworkclass", "3Serieswith2reworkclasses"],
    "multiclass": ["2CSS","2CDS","3Serieswithreworkclass", "3Serieswith2reworkclasses", "2CDSDC"],
    "parallel": ["ParallelQueues","ParallelErlang", "ParallelServers", "ParallelService"],
    "linear": ["3Series", "3Closed", "3Serieswithrework", "3Serieswithreworkclass", "3Serieswith2reworkclasses"],
    "erlang": ["ParallelErlang", "Burst", "ParallelService"],
    "multiserver": ["ParallelServers", "Burst", "MM2HighU"],
    "high_utilization": ["MM1HighU", "MM2HighU", "ParallelErlang", "3Closed", "ParallelServers", "MM110", "ParallelService", "3Serieswithrework",
                        "3Serieswithreworkclass", "3Serieswith2reworkclasses", "2CDSDC"]
}

correct_q = {
    "MM1LowU": [0.25], 
    "MM1HighU": [4], 
    "MM2HighU": [4.444], 
    "MM110": [2.968], 
    "ParallelService": [3.242, 3.176,3.975],
    "ParallelQueues": [0.286, 0.32, 0.364], 
    "2CDSDC": [2.131,1.165],
    "Burst": [2.995,3.014,3.423,3.008,3.007,1.546,3.443,0.6,1.546,1.501], 
    "ParallelServers": [5.45, 6.01, 3.03], 
    "3Serieswith2reworkclasses": [2.290, 0.176,0.029, 3.234,0.279,0.36, 5.433,0.486,0.055], 
    "3Serieswithreworkclass": [2.185,0.185, 3.02, 0.289, 4.896,0.487], 
    "ParallelErlang": [3.962, 6.963, 3.449], 
    "2CDS": [29.98,18.62], 
    "2CSS": [8,4],
    "3Serieswithrework": [2.862, 4.2, 7.964], 
    "3Series": [2, 2.668, 3.993],
    "3Closed": [7.044, 4.567, 3.229]
}

id_cols = ["model_name", "variable", "order", "state", "tau"]
config_cols = ["variable", "order", "state"]

if __name__=="__main__":
    all_strats = pd.DataFrame()
    class_strats = {
        class_model: pd.DataFrame() for class_model in class_models.keys()
    }
    non_class_strats = {
        class_model: pd.DataFrame() for class_model in class_models.keys()
    }
    strat_detail = {
        "variable": pd.DataFrame(),
        "order": pd.DataFrame(),
        "state": pd.DataFrame()
    }

    class_detail = {
        "variable": {
            class_model: pd.DataFrame() for class_model in class_models.keys()
        },
        "order": {
            class_model: pd.DataFrame() for class_model in class_models.keys()
        },
        "state": {
            class_model: pd.DataFrame() for class_model in class_models.keys()
        }
    }
    non_class_detail = {
        "variable": {
            class_model: pd.DataFrame() for class_model in class_models.keys()
        },
        "order": {
            class_model: pd.DataFrame() for class_model in class_models.keys()
        },
        "state": {
            class_model: pd.DataFrame() for class_model in class_models.keys()
        }
    }
    for model_name in model_names:
        print(model_name)
        avg_frame = pd.read_csv("../TauLeapCombined/{}_avg.csv".format(model_name))
        dev_frame = pd.read_csv("../TauLeapCombined/{}_dev.csv".format(model_name))
        q_columns = [c for c in avg_frame.columns if c[-2:] == "_Q" and c[:5] not in ["Sourc", "Class", "Route"]]
        q_columns_dev = [c + "_dev" for c in q_columns]
        dev_frame[q_columns_dev] = dev_frame[q_columns]
        avg_frame = avg_frame[id_cols + q_columns + [" solver_time"]]
        avg_frame[q_columns_dev] = dev_frame[q_columns_dev]
        mse_cols = []
        avg_frame["Total_MSE"] = 0
        avg_frame["Total_Var"] = 0
        for i, col in enumerate(q_columns):
            real_ans = correct_q[model_name][i]
            avg_frame[col + "_real"] = real_ans
            avg_frame[col + "_bias2"] = (avg_frame[col]*avg_frame[col]) + (real_ans**2) - (2*real_ans*avg_frame[col])
            avg_frame[col + "_10MSE"] = (avg_frame[col + "_dev"]/(10**(1/2)))**2 + avg_frame[col + "_bias2"]
            mse_cols.append(col + "_10_MSE")
            avg_frame["Total_MSE"] += avg_frame[col + "_bias2"] + avg_frame[col + "_10MSE"]
            avg_frame["Total_Var"] += (avg_frame[col + "_dev"]**2)

        total_mse_frame = avg_frame.groupby(config_cols)["Total_MSE"].min().reset_index()
        all_strats = all_strats.append(total_mse_frame)

        for config_col in config_cols:
            strat_detail[config_col] = strat_detail[config_col].append(avg_frame.groupby(config_col)["Total_MSE"].min().reset_index())
            for class_model, rel_models in class_models.items():
                if model_name in rel_models:
                    class_detail[config_col][class_model] = class_detail[config_col][class_model].append(avg_frame.groupby(config_col)["Total_MSE"].min().reset_index())
                else:
                    non_class_detail[config_col][class_model] = non_class_detail[config_col][class_model].append(avg_frame.groupby(config_col)["Total_MSE"].min().reset_index())
        for class_model, rel_models in class_models.items():
            if model_name in rel_models:
                class_strats[class_model] = class_strats[class_model].append(total_mse_frame)
            else:
                non_class_strats[class_model] = non_class_strats[class_model].append(total_mse_frame)

        avg_frame = avg_frame[avg_frame["Total_MSE"]<50]
        avg_frame = avg_frame.sort_values(by=" solver_time").reset_index()
        avg_frame["Min_MSE"] = avg_frame["Total_MSE"]
        for i, row in avg_frame.iterrows():
            if i == 0:
                continue
            avg_frame["Min_MSE"].loc[i] = avg_frame["Total_MSE"].loc[:i].min()
        avg_frame = avg_frame[avg_frame["Total_MSE"] <= avg_frame["Min_MSE"]]
        avg_frame = avg_frame.sort_values(by="Total_MSE", ascending=True).drop("Min_MSE", axis=1).reset_index()
        maximized_ns = []
        ivals = avg_frame.index
        i_idx = 0
        while i_idx < len(ivals):
            i = avg_frame.index[i_idx]
            row = avg_frame.loc[i]
            total_bias = 0
            for col in q_columns:
                total_bias += row[col + "_bias2"]
                
            for j, row2 in avg_frame.loc[(i+1):].iterrows():
                if row2["Total_MSE"] < total_bias:
                    continue
                for possible_n in range(1,10):
                    if ((row[" solver_time"]*(possible_n/10)) > row2[" solver_time"]):
                        break

                    new_mse = total_bias
                    for col in q_columns:
                        new_mse += (row[col + "_dev"]/(possible_n**(1/2)))**2
                    if new_mse < row2["Total_MSE"]:
                        maximized_ns.append(j)
                        break
            n_removed_before = 0
            for j, row2 in avg_frame.loc[:i].iterrows():
                alt_mse = row2["Total_MSE"]
                if total_bias > alt_mse:
                    continue
                for possible_n in range(11,100):
                    if (row[" solver_time"]*(possible_n/10) > row2[" solver_time"]):
                        break

                    new_mse = total_bias
                    for col in q_columns:
                        new_mse += (row[col + "_dev"]/(possible_n**(1/2)))**2
                    if new_mse < alt_mse:
                        maximized_ns.append(j)
                        n_removed_before += 1
                        break
            avg_frame = avg_frame.drop(maximized_ns)
            maximized_ns = []
            i_idx += 1
            i_idx -= n_removed_before
            ivals = avg_frame.index

        print(avg_frame[id_cols + [" solver_time", "Total_MSE"]])
    #all_strats = all_strats.groupby(config_cols).sum().reset_index()
    #print(all_strats.sort_values("Total_MSE"))
    #for config_col in config_cols:
    #    print("Config: ", config_col)
    #    strat_detail[config_col] = strat_detail[config_col].groupby(config_col).sum().reset_index()
    #    print(strat_detail[config_col].sort_values("Total_MSE"))
    #for model_class in class_strats:
    #    class_strats[model_class] = class_strats[model_class].groupby(config_cols).sum().reset_index()
    #    non_class_strats[model_class] = non_class_strats[model_class].groupby(config_cols).sum().reset_index()
    #    print("Strat for ", model_class)
    #    print(class_strats[model_class].sort_values("Total_MSE"))
    #    print("Without:")
    #    print(non_class_strats[model_class].sort_values("Total_MSE"))

    #    for config_col in config_cols:
    #        print("Config: ", config_col)
    #        class_detail[config_col][model_class] =class_detail[config_col][model_class].groupby(config_col).sum().reset_index()
    #        non_class_detail[config_col][model_class] = non_class_detail[config_col][model_class].groupby(config_col).sum().reset_index()
    #        print(class_detail[config_col][model_class].sort_values("Total_MSE"))
    #        print("Without:")
    #        print(non_class_detail[config_col][model_class].sort_values("Total_MSE"))