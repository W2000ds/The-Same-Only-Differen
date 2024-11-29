This is our performance test template and the actual test code of MySQL under sysbench.

### Test template

```python

modify_config(config_file, parameter, value):
    stop_sys()

    # modify configuration value
    config.set(section, parameter, value)

    start_sys()

    if error occur:
        print(f"error：{error}")
        return False


stop_sys(){
	//stop system
}
   

start_sys(){
	//start system
}
  


run_bench(workload_factors):

    commands = {"your bench run command"}

    result = subprocess.run(command)
    return result.stdout



extract_metrics(bench_output):

    pattern = r"\\\\\"
    metric = pattern.bench_output

    return metric
	

clearconf(config_file):
    //change config file to default


    

main():

        config_file = "path to your config file"

        confdata = read_csv_to_dict("path to your test file")

        workload_factors = "your workload"


        for conf in confdata:
            clearconf(config_file)

            try:
                if(modify_config(config_file, conf['optionname'], conf['value']):
                    continue

                bench_output = run_bench(workload_factors)
                metric = extract_metrics(bench_output)

                results.append([conf['optionname'], conf['value'], metric)

            except Exception as e:
                print(f"---error---: {e}")
                continue


```





### MySQL-Sysbench.py


```python
import configparser
import subprocess
import re
import subprocess
import csv
import numpy as np

def modify_mysql_config(config_file, section, parameter, value, sys_password):
    try:

        config = configparser.RawConfigParser()


        chmod_777(config_file, sys_password)


        config.read(config_file)


        config.set(section, parameter, value)


        with open(config_file, 'w') as f:
            config.write(f)

        print(f"modify {parameter} to {value}")


        chmod_644(config_file, sys_password)


        restart_mysql_service(sys_password)
        return True
    except Exception as error:
        print(f"error：{error}")
        return False


def restart_mysql_service(sys_password):

    command = f"echo {sys_password} | sudo -S systemctl restart mysql.service"

    subprocess.run(command, shell=True, check=True, executable="/bin/bash")


def chmod_777(file_path, sudo_password):

    command = f"echo {sudo_password} | sudo -S chmod 777 {file_path}"


    subprocess.run(command, shell=True, check=True, executable="/bin/bash")


def chmod_644(file_path, sudo_password):

    command = f"echo {sudo_password} | sudo -S chmod 644 {file_path}"


    subprocess.run(command, shell=True, check=True, executable="/bin/bash")


def run_sysbench(mysql_host, mysql_port, mysql_user, mysql_password, mysql_db, threads=12, time=40, report_interval=1, tables=24, table_size=100000):
    base_command = f"echo 777777 | sudo -S sysbench --threads={threads} --time={time} --report-interval={report_interval} --mysql-host={mysql_host} --mysql-port={mysql_port} --mysql-user={mysql_user} --mysql-password={mysql_password} --mysql-db={mysql_db} --tables={tables} --table-size={table_size}"

    commands = {
        "prepare": f"{base_command} /usr/share/sysbench/oltp_common.lua prepare",
        "run": f"{base_command} oltp_read_write run",
        "cleanup": f"{base_command} /usr/share/sysbench/oltp_common.lua cleanup"
    }

    for stage, command in commands.items():
        print(f"Sysbench {stage}...")
        try:
            if stage == "run":
                result = subprocess.run(command, shell=True, check=True, stdout=subprocess.PIPE,stderr=subprocess.STDOUT, text=True)
            else:
                cmdresult = subprocess.run(command, shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
            print(cmdresult.stdout)
        except subprocess.CalledProcessError as e:
            print(f"errorlog：{e.output}")
    return result.stdout

def extract_metrics(sysbench_output):

    tps_pattern = r"transactions:\s*\d+\s*\((\d+\.\d+) per sec.\)"
    qps_pattern = r"queries:\s*\d+\s*\((\d+\.\d+) per sec.\)"
    avg_latency_pattern = r"avg:\s+(\d+\.\d+)"


    tps_matches = re.search(tps_pattern, sysbench_output)
    tps = tps_matches.group(1) if tps_matches else "not found"


    qps_matches = re.search(qps_pattern, sysbench_output)
    qps = qps_matches.group(1) if qps_matches else "not found"


    avg_latency_matches = re.search(avg_latency_pattern, sysbench_output)
    avg_latency = avg_latency_matches.group(1) if avg_latency_matches else "not found"

    return tps, qps, avg_latency

def read_csv_to_dict(filename):
    data = []
    with open(filename, mode='r', encoding='utf-8') as file:
        reader = csv.DictReader(file)
        for row in reader:

            data.append(row)
    return data

def clearconf(config_file):
    command = f'echo {sys_password} | sudo -S bash -c "echo \'[mysqld]\' > {config_file}"'
    chmod_777(config_file, sys_password)
    subprocess.run(command, shell=True, check=True, executable="/bin/bash")
    chmod_644(config_file, sys_password)


def calculate_change(current, standard):

    return ((current - standard) / standard) * 100 if standard != 0 else 0


def is_outlier(points, thresh=2.0):
    if len(points) == 1:
        return False
    median = np.median(points)
    diff = np.abs(points - median)
    mdev = np.median(diff)
    modified_z_score = 0.6745 * diff / mdev
    return modified_z_score > thresh


if __name__ == '__main__':

        config_file = "/etc/mysql/conf.d/mysql.cnf"
        section = 'mysqld'
        sys_password = "777777"
        confdata = read_csv_to_dict("mysqltest.csv")

        results = []


        for conf in confdata:
            #clear conf file
            clearconf(config_file)

            try:
                tpssum = 0.0
                qpssum = 0.0
                latencysum = 0.0

                tps_list = []
                qps_list = []
                latency_list = []

                if(modify_mysql_config(config_file, section, conf['optionname'], conf['value'], sys_password)==False):
                    continue
                # run for 5 times
                for i in range(1, 5):
                    sysbench_output = run_sysbench(
                        mysql_host="localhost",
                        mysql_port=3306,
                        mysql_user="root",
                        mysql_password="123456",
                        mysql_db="sbtest"
                    )
                    tps, qps, latency = extract_metrics(sysbench_output)
                    tps = float(tps)
                    qps = float(qps)
                    latency = float(latency)
                    latency_list.append(latency)
                    print(f"{i}th TPS: {tps}, QPS: {qps}, avg latency: {latency} ms")


                results.append([conf['optionname'], conf['valuename'], conf['value'], tps,qps,latency])
                with open('result.csv', 'w', newline='') as file:
                    writer = csv.writer(file)
                    writer.writerows(results)

            except Exception as e:
                print(f"---error---: {e}")
                continue


