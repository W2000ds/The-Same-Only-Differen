This is our performance test template and the actual test code of all systems.

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
```

### MySQL-tpcc.py

```python
import configparserimport subprocess
import re
import csv
import numpy as np

def modify_mysql_config(config_file, section, parameter, value, sys_password):
    try:
        # Create a RawConfigParser object
        config = configparser.RawConfigParser()

        # Grant write permissions
        chmod_777(config_file, sys_password)

        # Read MySQL configuration file
        config.read(config_file)

        # Modify the parameter value
        config.set(section, parameter, value)

        # Save the modified configuration file
        with open(config_file, 'w') as f:
            config.write(f)

        print(f"Successfully modified parameter {parameter} to {value}")

        # Revoke write permissions
        chmod_644(config_file, sys_password)

        # Restart MySQL service
        restart_mysql_service(sys_password)
        return True
    except Exception as error:
        print(f"Modification failed: {error}")
        return False


def restart_mysql_service(sys_password):
    # Use subprocess module to restart MySQL service
    command = f"echo {sys_password} | sudo -S systemctl restart mysql.service"
    subprocess.run(command, shell=True, check=True, executable="/bin/bash")


def chmod_777(file_path, sudo_password):
    # Build chmod command
    command = f"echo {sudo_password} | sudo -S chmod 777 {file_path}"

    # Execute command
    subprocess.run(command, shell=True, check=True, executable="/bin/bash")


def chmod_644(file_path, sudo_password):
    # Build chmod command
    command = f"echo {sudo_password} | sudo -S chmod 644 {file_path}"

    # Execute command
    subprocess.run(command, shell=True, check=True, executable="/bin/bash")


def run_sysbench(mysql_host, mysql_port, mysql_user, mysql_password, mysql_db, threads=8, time=40, report_interval=1, tables=8, table_size=100000):
    commands = {
        "run": f"tpcc_start -h127.0.0.1 -P3306 -d sbtest -u root -p password -w 4 -c 4 -r 3 -l 20",
    }

    for stage, command in commands.items():
        print(f"Starting Sysbench {stage}...")
        try:
            result = subprocess.run(command, shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
            print(result.stdout)
            return result.stdout
        except subprocess.CalledProcessError as e:
            print(f"Error log: {e.output}")
            return "error"


def extract_metrics(sysbench_output):
    # Define regular expression pattern for extracting metrics
    TpmC_pattern = r"<TpmC>\s+([\d.]+)\s+TpmC"

    # Extract TpmC value
    TpmC_matches = re.search(TpmC_pattern, sysbench_output)
    TpmC = TpmC_matches.group(1) if TpmC_matches else "Not found"

    return TpmC


def read_csv_to_dict(filename):
    # Read CSV file and convert it into a list of dictionaries
    data = []
    with open(filename, mode='r', encoding='utf-8') as file:
        reader = csv.DictReader(file)
        for row in reader:
            data.append(row)
    return data


def clearconf(config_file):
    # Clear the MySQL configuration file
    command = f'echo {sys_password} | sudo -S bash -c "echo \'[mysqld]\' > {config_file}"'
    chmod_777(config_file, sys_password)
    subprocess.run(command, shell=True, check=True, executable="/bin/bash")
    chmod_644(config_file, sys_password)




if __name__ == '__main__':

    config_file = "/etc/mysql/conf.d/mysql.cnf"
    section = 'mysqld'
    sys_password = "password"  # Replace with actual password or secure method
    confdata = read_csv_to_dict("config_file.csv")  # Replace with actual CSV file path
   

    for conf in confdata:
        # Clear configuration file
        clearconf(config_file)

        try:

            tps_list = []
            qps_list = []
            latency_list = []

            if not modify_mysql_config(config_file, section, conf['optionname'], conf['value'], sys_password):
                continue

            # Run the benchmark multiple times (adjust loop count as needed)
            for i in range(1, 6):
                sysbench_output = run_sysbench(
                    mysql_host="localhost",
                    mysql_port=3306,
                    mysql_user="root",
                    mysql_password="password",  # Replace with actual password
                    mysql_db="sbtest"
                )
                TpmC = extract_metrics(sysbench_output)
                TpmC = float(TpmC)
                latency_list.append(TpmC)
                print(f"{i}th TpmC: {TpmC} ms")

            results.append([conf['optionname'], conf['valuename'], conf['value'], TpmC])
            with open('results.csv', 'w', newline='') as file:
                writer = csv.writer(file)
                writer.writerows(results)

        except Exception as e:
            print(f"---error---: {e}")
            continue

```

### httpd-ApacheBench.py
```python
import csv
import subprocess
import numpy as np
import time
import re
import os

password = "your_password"  # Replace with actual password or secure method

# Define different workload parameter combinations
workloads = [
    (10000, 100),
    (10000, 1000),
    (1000, 100),
    (100, 10),
    (100000, 10000)
]

def run_httpd_benchmark(requests, concurrency):
    # Run httpd performance test command
    try:
        command = f"ab -n {requests} -c {concurrency} http://127.0.0.1:70/"
        result = subprocess.run(command, shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                                text=True, timeout=120)
    except Exception as e:
        print(e)
        return "error"
    return result.stdout

def extract_data_from_output(output):
    # Regular expressions to match key performance metrics
    requests_per_second_pattern = r"Requests per second:\s+(\d+\.\d+)"
    time_per_request_pattern = r"Time per request:\s+(\d+\.\d+)"

    # Extract metrics
    requests_per_second = re.search(requests_per_second_pattern, output).group(1)
    time_per_request = re.search(time_per_request_pattern, output).group(1)

    return requests_per_second, time_per_request

def clear_httpd_conf(file_path):
    with open(file_path, "w") as file:
        file.truncate()

def startsys():
    try:
        subprocess.run(f"echo {password} | sudo -S /usr/local/httpd/bin/apachectl start", shell=True, check=True,
                       stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    except Exception as e:
        print(e)
        return False
    time.sleep(2)  # give httpd 2 seconds to start
    return True

def stopsys():
    commandstop = f"echo {password} | sudo -S /usr/local/httpd/bin/apachectl stop"
    try:
        subprocess.run(commandstop, shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    except Exception as e:
        print(e)
    time.sleep(1)

def add_config_to_httpd_conf(file_path, configname, configvalue):
    commandwrite2file = f"echo '{configname} {configvalue}' > {file_path}"

    subprocess.run(commandwrite2file, shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                   text=True)

    if startsys():
        time.sleep(3)
        return True
    return False

def read_csv_to_dict(filename):
    data = []
    with open(filename, mode='r', encoding='utf-8') as file:
        reader = csv.DictReader(file)
        for row in reader:
            data.append(row)
    return data

def calculate_change(current, standard):
    """Calculate percentage change."""
    return ((current - standard) / standard) * 100 if standard != 0 else 0

if __name__ == '__main__':
    filepath = "/path/to/httpd.conf"  # Update the path as necessary
    confdata = read_csv_to_dict("/path/to/test.csv")  # Update the path as necessary

    # Replace actual data with placeholders
    RPS_stand = None  # Placeholder for actual standard RPS value
    TPR_stand = None  # Placeholder for actual standard TPR value

    for requests, concurrency in workloads:
        results_file = f"/path/to/results/Apache-bench-{requests}-{concurrency}.csv"  # Update the path as necessary

        # Check if the result file exists
        if os.path.exists(results_file):
            print(f"Result file for workload {requests}-{concurrency} already exists. Skipping...")
            continue

        print(f"Running workload: {requests} requests, {concurrency} concurrency")

        results = []
        clear_httpd_conf(filepath)

        for conf in confdata:
            stopsys()
            clear_httpd_conf(filepath)
            print(f"{conf['name']} {conf['valuename']} {conf['value']}")

            RPS_list = []
            TPR_list = []

            if not add_config_to_httpd_conf(filepath, conf['name'], conf['value']):
                continue

            for i in range(1, 2):
                bench_output = run_httpd_benchmark(requests, concurrency)
                if bench_output == "error":
                    break
                RPS, TPR = extract_data_from_output(bench_output)
                print(f"{i}th : Requests per second:{RPS}, Time per request: {TPR} ms")

                RPS_list.append(float(RPS))
                TPR_list.append(float(TPR))

            if bench_output == "error":
                results.append([conf['name'], conf['valuename'], conf['value'], 'timeout', 'timeout', 'timeout', 'timeout'])
                continue

            RPS_list = np.array(RPS_list)
            TPR_list = np.array(TPR_list)

            RPS_avg = np.mean(RPS_list) if len(RPS_list) else None
            TPR_avg = np.mean(TPR_list) if len(TPR_list) else None

            print(f"Average RPS: {RPS_avg}, Average TPR: {TPR_avg} ms")

            # Calculate the change if the standard values are provided
            if RPS_stand is not None and TPR_stand is not None:
                RPSchange = calculate_change(RPS_avg, RPS_stand)
                TPRchange = calculate_change(TPR_avg, TPR_stand)

                print(f"Average RPS change: {RPSchange}, Average TPR change: {TPRchange}")
            else:
                print("Standard values for RPS and TPR are not set. Skipping change calculation.")

            results.append([conf['name'], conf['valuename'], conf['value'], RPS_avg, TPR_avg, RPSchange, TPRchange])

            # Save results to CSV file
            with open(results_file, 'w', newline='') as file:
                writer = csv.writer(file)
                writer.writerow(['Name', 'ValueName', 'Value', 'Average RPS', 'Average TPR', 'RPS Change', 'TPR Change'])
                writer.writerows(results)

```

### httpd-Jmeter.py
```python
import csv
import subprocess
import numpy as np
import time
import re
import os

# Password for sudo operations
password = 'password'

# Function to run JMeter test with given system and test ID
def run_jmeter(sys, id):
    try:
        # Command to run JMeter
        command = f"/path/to/jmeter -n -t /path/to/jmeter/{sys}/{id}.jmx -l /path/to/results.jtl -j /path/to/jmeter.log"
        result = subprocess.run(command, shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                                text=True, timeout=20)
    except Exception as e:
        print(e)
        return "error"
    return result.stdout

# Function to extract throughput data from JMeter output
def extract_data_from_output(output):
    throughput_pattern = r"summary =\s+\d+\s+in\s+\d+:\d+:\d+\s+=\s+([\d.]+)/s"
    throughput = re.search(throughput_pattern, output).group(1)
    return float(throughput)

# Function to add configuration to a configuration file
def add_config_to_conf(file_path, configname, configvalue):
    commandwrite2file = f"echo '{configname} {configvalue}' > {file_path}"
    subprocess.run(commandwrite2file, shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                   text=True)
    # Start the system after modifying the config file
    if start_sys():
        time.sleep(3)
        return True
    return False

# Function to start the system
def start_sys():
    try:
        # Start the system (example using Apache HTTPD)
        subprocess.run(f"echo {password} | sudo -S /path/to/apachectl start", shell=True, check=True,
                       stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    except Exception as e:
        print(e)
        return False
    time.sleep(2)  # Give the system 2 seconds to start
    return True

# Function to stop the system
def stop_sys():
    commandstop = f"echo {password} | sudo -S /path/to/apachectl stop"
    try:
        subprocess.run(commandstop, shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    except Exception as e:
        print(e)
    time.sleep(1)

# Function to read a CSV file into a dictionary
def read_csv_to_dict(filename):
    data = []
    with open(filename, mode='r', encoding='utf-8') as file:
        reader = csv.DictReader(file)
        for row in reader:
            data.append(row)
    return data

# Function to clear a configuration file by truncating it
def clear_conf(file_path):
    with open(file_path, "w") as file:
        file.truncate()

# Function to calculate percentage change
def calculate_change(current, standard):
    return ((current - standard) / standard) * 100 if standard != 0 else 0

# Main function to run the workload
if __name__ == '__main__':
    filepath = "/path/to/httpd.conf"
    confdata = read_csv_to_dict("/path/to/config.csv")
    sys = "httpd"

    for id in range(1, 3):
        results_file = f"/path/to/results/{sys}-jmeter-{id}.csv"
        
        # Stop the system before starting the test
        stop_sys()
        clear_conf(filepath)
        start_sys()
        
        throughput_list = []
        # Run the benchmark 5 times to get throughput data
        for i in range(1, 6):
            bench_output = run_jmeter(sys, id)
            throughput = extract_data_from_output(bench_output)
            throughput_list.append(float(throughput))
        
        throughput_list = np.array(throughput_list)
        throughput_avg = np.mean(throughput_list) if len(throughput_list) else None

        # Check if results file exists
        if os.path.exists(results_file):
            print(f"Result file for workload {id} already exists. Skipping...")
            continue

        print(f"Running workload: {id}")
        results = []
        clear_conf(filepath)
        
        for conf in confdata:
            stop_sys()
            print(f"Modifying {conf['name']} to {conf['value']}")

            if not add_config_to_conf(filepath, conf['name'], conf['value']):
                results.append([conf['name'], conf['valuename'], conf['value'], 'error', 'error'])
                continue

            bench_output = run_jmeter(sys, id)
            if bench_output == "error":
                results.append([conf['name'], conf['valuename'], conf['value'], 'timeout', 'timeout'])
                continue

            throughput = extract_data_from_output(bench_output)
            print(f"Throughput: {throughput}")

            throughput_change = calculate_change(throughput, throughput_avg)

            results.append([conf['name'], conf['valuename'], conf['value'], throughput, throughput_change])

            # Save the results to a CSV file
            with open(results_file, 'w', newline='') as file:
                writer = csv.writer(file)
                writer.writerow(['Name', 'ValueName', 'Value', 'Throughput', 'ThroughputChange'])
                writer.writerows(results)
```

### nginx-ApacheBench.py
```python
import csv
import subprocess
import numpy as np
import time
import re
import shutil
import os
import xml.etree.ElementTree as ET

# Password for sudo commands
password = 'your_password_here'

# Define different workload parameter combinations
def run_jmeter(sys, id):
    """Run JMeter benchmark test."""
    try:
        command = f"jmeter -n -t {sys}/{id}.jmx -l results.jtl -j jmeter.log"
        result = subprocess.run(command, shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                                text=True, timeout=20)
    except Exception as e:
        print(e)
        return "error"
    return result.stdout

def extract_data_from_output(output):
    """Extract throughput value from the JMeter output."""
    throughput_pattern = r"summary =\s+\d+\s+in\s+\d+:\d+:\d+\s+=\s+([\d.]+)/s"
    throughput = re.search(throughput_pattern, output).group(1)
    return float(throughput)

def reload_sys():
    """Reload the system configuration for Nginx."""
    try:
        subprocess.run(f"echo {password} | sudo -S nginx -s reload", shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    except Exception as e:
        print(e)
        return False
    time.sleep(2)  # give Nginx 2 seconds to reload
    return True

def add_config_to_conf(file_path, configname, configvalue):
    """Add configuration value to the Nginx config file."""
    commandwrite2file = f"echo '{configname} {configvalue};' > {file_path}"
    subprocess.run(commandwrite2file, shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)

    if reload_sys():
        return True
    return False

def start_sys():
    """Start the Nginx service."""
    try:
        subprocess.run(f"echo {password} | sudo -S nginx", shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    except Exception as e:
        print(e)
        return False
    time.sleep(2)  # give Nginx 2 seconds to start
    return True

def read_csv_to_dict(filename):
    """Read CSV file into a list of dictionaries."""
    data = []
    with open(filename, mode='r', encoding='utf-8') as file:
        reader = csv.DictReader(file)
        for row in reader:
            data.append(row)
    return data

def clear_conf(file_path):
    """Clear the content of the Nginx configuration file."""
    with open(file_path, "w") as file:
        file.truncate()

def calculate_change(current, standard):
    """Calculate the percentage change."""
    return ((current - standard) / standard) * 100 if standard != 0 else 0

if __name__ == '__main__':
    # Define the path to the configuration file
    filepath = "path_to_nginx.conf"
    confdata = read_csv_to_dict("path_to_csv_file.csv")
    sys = "Nginx"

    for id in range(1, 3):
        results_file = f"nginx-bench-{id}.csv"
        start_sys()
        throughput_list = []
        
        # Run the benchmark 5 times
        for i in range(1, 6):
            bench_output = run_jmeter(sys, id)
            throughput = extract_data_from_output(bench_output)
            throughput_list.append(float(throughput))
        
        throughput_list = np.array(throughput_list)
        throughput_avg = np.mean(throughput_list) if len(throughput_list) else None

        # Check if result file already exists
        if os.path.exists(results_file):
            print(f"Result file for workload {id} already exists. Skipping...")
            continue

        print(f"Running workload: {id}")
        results = []
        clear_conf(filepath)
        
        # Modify configuration and run benchmarks
        for conf in confdata:
            print(f"Modifying {conf['name']} to {conf['value']}")

            if not add_config_to_conf(filepath, conf['name'], conf['value']):
                results.append([conf['name'], conf['valuename'], conf['value'], 'error', 'error'])
                continue

            bench_output = run_jmeter(sys, id)
            if bench_output == "error":
                results.append([conf['name'], conf['valuename'], conf['value'], 'timeout', 'timeout'])
                continue

            throughput = extract_data_from_output(bench_output)

            print(f"Throughput: {throughput}")

            throughputchange = calculate_change(throughput, throughput_avg)

            results.append([conf['name'], conf['valuename'], conf['value'], throughput, throughputchange])

            # Save results to CSV file
            with open(results_file, 'w', newline='') as file:
                writer = csv.writer(file)
                writer.writerow(['Name', 'ValueName', 'Value', 'Throughput', 'ThroughputChange'])
                writer.writerows(results)
```

### nginx-Jmeter.py
```python
import csv
import subprocess
import numpy as np
import time
import re
import os

# Define different workload parameter combinations
workloads = [
    (1000, 100),
    (10000, 100),
    (10000, 1000),
    (1000, 10),
]

def run_nginx_benchmark(requests, concurrency):
    # Run nginx performance test command
    try:
        command = f"ab -n {requests} -c {concurrency} http://127.0.0.1:90/test.html"
        result = subprocess.run(command, shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, timeout=10)
    except Exception as e:
        print(e)
        return "error"
    return result.stdout

def extract_data_from_output(output):
    # Use regular expressions to match key performance metrics
    requests_per_second_pattern = r"Requests per second:\s+(\d+\.\d+)"
    time_per_request_pattern = r"Time per request:\s+(\d+\.\d+)"

    # Extract metrics
    requests_per_second = re.search(requests_per_second_pattern, output).group(1)
    time_per_request = re.search(time_per_request_pattern, output).group(1)

    return requests_per_second, time_per_request

def clear_nginx_conf(file_path):
    # Clear the nginx configuration file
    with open(file_path, "w") as file:
        file.truncate()

def startsys():
    # Start the nginx server
    try:
        subprocess.run(f"echo {password} | sudo -S /path/to/nginx-server/sbin/nginx", shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    except Exception as e:
        print(e)
        return False
    time.sleep(2)  # give nginx 2 sec to start
    return True

def reloadsys():
    # Reload nginx server
    try:
        subprocess.run(f"echo {password} | sudo -S /path/to/nginx-server/sbin/nginx -s reload", shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    except Exception as e:
        print(e)
        return False
    time.sleep(2)  # give nginx 2 sec to reload
    return True

def stopsys():
    # Stop nginx server
    commandstop = f"echo {password} | /path/to/nginx-server/sbin/nginx -s stop"
    try:
        subprocess.run(commandstop, shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    except Exception as e:
        print(e)

def add_config_to_nginx_conf(file_path, configname, configvalue):
    # Add configuration to nginx config file
    commandwrite2file = f"echo '{configname} {configvalue};' > {file_path}"
    subprocess.run(commandwrite2file, shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)

    # Reload the nginx server to apply changes
    if reloadsys():
        return True
    return False

def read_csv_to_dict(filename):
    # Read a CSV file into a list of dictionaries
    data = []
    with open(filename, mode='r', encoding='utf-8') as file:
        reader = csv.DictReader(file)
        for row in reader:
            data.append(row)
    return data

def calculate_change(current, standard):
    # Calculate percentage change
    return ((current - standard) / standard) * 100 if standard != 0 else 0

if __name__ == '__main__':
    # Set the path for the nginx config file
    filepath = "/path/to/nginx.conf"
    confdata = read_csv_to_dict("/path/to/newtest.csv")
    



    for requests, concurrency in workloads:
        # Define the result file for the current workload
        results_file = f"/path/to/nginx-bench-{requests}-{concurrency}.csv"

        # Check if the result file already exists
        if os.path.exists(results_file):
            print(f"Result file for workload {requests}-{concurrency} already exists. Skipping...")
            continue

        print(f"Running workload: {requests} requests, {concurrency} concurrency")

        results = []
        clear_nginx_conf(filepath)

        for conf in confdata:
            startsys()
            clear_nginx_conf(filepath)
            print(f"{conf['name']} {conf['valuename']} {conf['value']}")

            RPS_list = []
            TPR_list = []

            # Add the configuration to nginx and reload the server
            if not add_config_to_nginx_conf(filepath, conf['name'], conf['value']):
                results.append([conf['name'], conf['valuename'], conf['value'], 'error', 'error'])
                continue

            # Run the benchmark and extract data
            for i in range(1, 5):
                bench_output = run_nginx_benchmark(requests, concurrency)
                if bench_output == "error":
                    break
                RPS, TPR = extract_data_from_output(bench_output)
                print(f"{i}th : Requests per second:{RPS}, Time per request: {TPR} ms")
                RPS_list.append(float(RPS))
                TPR_list.append(float(TPR))

            if bench_output == "error":
                results.append([conf['name'], conf['valuename'], conf['value'], 'timeout', 'timeout', 'timeout', 'timeout'])
                continue

            RPS_list = np.array(RPS_list)
            TPR_list = np.array(TPR_list)

            RPS_avg = np.mean(RPS_list) if len(RPS_list) else None
            TPR_avg = np.mean(TPR_list) if len(TPR_list) else None

            print(f"Average RPS: {RPS_avg}, Average TPR: {TPR_avg} ms")


            print(f"Average RPS change: {RPSchange}, Average TPR change: {TPRchange}")
            results.append([conf['name'], conf['valuename'], conf['value'], RPS_avg, TPR_avg])

        # Save results to CSV file
        with open(results_file, 'w', newline='') as file:
            writer = csv.writer(file)
            writer.writerow(['Name', 'ValueName', 'Value', 'Average RPS', 'Average TPR', 'RPS Change', 'TPR Change'])
            writer.writerows(results)
```

### lighttpd-Jmeter.py
```python
import csv
import subprocess
import numpy as np
import time
import re
import os

# Define password (remove it in the real code for security)
password = 'password_here'


# Function to run JMeter benchmark
def run_jmeter(sys, id):
    try:
        # JMeter command with dynamic arguments
        command = f"/path/to/jmeter/bin/jmeter -n -t /path/to/jmeter/{sys}/{id}.jmx -l /path/to/jmeter/results.jtl -j /path/to/jmeter/jmeter.log"

        result = subprocess.run(command, shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                                text=True, timeout=20)
    except Exception as e:
        print(e)
        return "error"
    return result.stdout


# Extract throughput data from JMeter output
def extract_data_from_output(output):
    throughput_pattern = r"summary =\s+\d+\s+in\s+\d+:\d+:\d+\s+=\s+([\d.]+)/s"
    throughput = re.search(throughput_pattern, output).group(1)
    return float(throughput)


# Function to modify server configuration file
def add_config_to_conf(file_path, configname, configvalue):
    commandwrite2file = f"echo '{configname} = {configvalue}' > {file_path}"

    subprocess.run(commandwrite2file, shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)

    if start_sys():
        return True
    return False


# Start the server
def start_sys():
    try:
        subprocess.run(f"echo {password} | sudo -S /path/to/lighttpd -f /path/to/config/lighttpd.conf", shell=True, check=True,
                       stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    except Exception as e:
        print(e)
        return False
    time.sleep(2)  # Give lighttpd 2 seconds to start
    return True

# Stop the server
def stop_sys():
    commandstop = f"echo {password} | sudo -S killall lighttpd"

    try:
        subprocess.run(commandstop, shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    except Exception as e:
        print(e)


# Read CSV file into dictionary
def read_csv_to_dict(filename):
    data = []
    with open(filename, mode='r', encoding='utf-8') as file:
        reader = csv.DictReader(file)
        for row in reader:
            data.append(row)
    return data

# Clear the configuration file
def clear_conf(file_path):
    with open(file_path, "w") as file:
        file.truncate()

# Calculate the percentage change from the standard value
def calculate_change(current, standard):
    return ((current - standard) / standard) * 100 if standard != 0 else 0


# Main execution block
if __name__ == '__main__':
    # Configuration file path and data source
    filepath = "/path/to/config/config.conf"
    confdata = read_csv_to_dict("/path/to/data/config.csv")
    sys = "lighttpd"

    # Loop through test IDs
    for id in range(1, 3):
        results_file = f"/path/to/results/{sys}-jmeter-{id}.csv"
        stop_sys()
        clear_conf(filepath)
        start_sys()
        throughput_list = []
        
        # Run the benchmark 5 times to gather throughput data
        for i in range(1, 6):
            bench_output = run_jmeter(sys, id)
            throughput = extract_data_from_output(bench_output)
            throughput_list.append(float(throughput))
        throughput_list = np.array(throughput_list)
        throughput_avg = np.mean(throughput_list) if len(throughput_list) else None

        # Check if the result file already exists
        if os.path.exists(results_file):
            print(f"Result file for workload {id} already exists. Skipping...")
            continue

        print(f"Running workload: {id}")
        results = []
        clear_conf(filepath)

        # Loop through configuration data
        for conf in confdata:
            stop_sys()
            print(f"Modifying {conf['name']} to {conf['value']}")

            if not add_config_to_conf(filepath, conf['name'], conf['value']):
                results.append([conf['name'], conf['valuename'], conf['value'], 'error', 'error'])
                continue

            bench_output = run_jmeter(sys, id)
            if bench_output == "error":
                results.append([conf['name'], conf['valuename'], conf['value'], 'timeout', 'timeout'])
                continue

            throughput = extract_data_from_output(bench_output)

            print(f"Throughput: {throughput}")

            throughput_change = calculate_change(throughput, throughput_avg)

            results.append([conf['name'], conf['valuename'], conf['value'], throughput, throughput_change])

            # Save the results to CSV file
            with open(results_file, 'w', newline='') as file:
                writer = csv.writer(file)
                writer.writerow(['Name', 'ValueName', 'Value', 'Throughput', 'ThroughputChange'])
                writer.writerows(results)

```


### lighttpd-ApacheBench.py
```python
import csv
import subprocess
import numpy as np
import time
import re
import os

# System password for sudo access
password = "your_sudo_password"

# Define workload configurations
workloads = [
    (10000, 100),
    (10000, 1000),
    (1000, 100),
    (100, 10),
    (100000, 10000)
]

# Function to run the benchmark with specific requests and concurrency
def run_lighttpd_benchmark(requests, concurrency):
    try:
        command = f"ab -n {requests} -c {concurrency} http://127.0.0.1:70/test.html"
        result = subprocess.run(command, shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                                text=True, timeout=10)
    except Exception as e:
        print(e)
        return "error"
    return result.stdout

# Function to extract relevant performance data from the benchmark output
def extract_data_from_output(output):
    lines = output.split('\n')

    # Regular expressions to match key performance indicators
    requests_per_second_pattern = r"Requests per second:\s+(\d+\.\d+)"
    time_per_request_pattern = r"Time per request:\s+(\d+\.\d+)"

    # Extract performance metrics
    requests_per_second = re.search(requests_per_second_pattern, output).group(1)
    time_per_request = re.search(time_per_request_pattern, output).group(1)

    return requests_per_second, time_per_request

# Function to clear the lighttpd configuration file
def clear_lighttpd_conf(file_path):
    with open(file_path, "w") as file:
        file.truncate()

# Function to start the lighttpd service
def startsys():
    try:
        subprocess.run(f"echo {password} | sudo -S /usr/local/sbin/lighttpd -f /home/lhy/lighttpd-server/config/lighttpd.conf", shell=True, check=True,
                       stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    except Exception as e:
        print(e)
        return False
    time.sleep(2)  # Give lighttpd 2 seconds to start
    return True

# Function to stop the lighttpd service
def stopsys():
    commandstop = f"echo {password} | sudo -S killall lighttpd"

    try:
        subprocess.run(commandstop, shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    except Exception as e:
        print(e)

# Function to add a configuration value to the lighttpd configuration file
def add_config_to_lighttpd_conf(file_path, configname, configvalue):
    commandwrite2file = f"echo '{configname} = {configvalue}' > {file_path}"

    subprocess.run(commandwrite2file, shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)

    if startsys():
        return True
    return False

# Function to read a CSV file and convert its rows into dictionaries
def read_csv_to_dict(filename):
    data = []
    with open(filename, mode='r', encoding='utf-8') as file:
        reader = csv.DictReader(file)
        for row in reader:
            data.append(row)
    return data

# Function to calculate the percentage change between current and standard values
def calculate_change(current, standard):
    return ((current - standard) / standard) * 100 if standard != 0 else 0

# Main function
if __name__ == '__main__':
    filepath = "/path/to/config.conf"  # Path to the configuration file
    confdata = read_csv_to_dict("/path/to/test.csv")  # Path to the CSV file with configurations

    # Loop through each workload configuration
    for requests, concurrency in workloads:
        results_file = f"/path/to/results/Apache-bench-{requests}-{concurrency}.csv"  # Define the results file

        # Check if the results file already exists, if so, skip
        if os.path.exists(results_file):
            print(f"Result file for workload {requests}-{concurrency} already exists. Skipping...")
            continue

        print(f"Running workload: {requests} requests, {concurrency} concurrency")

        results = []
        clear_lighttpd_conf(filepath)

        # Loop through each configuration in the CSV data
        for conf in confdata:
            stopsys()
            clear_lighttpd_conf(filepath)
            print(f"{conf['name']} {conf['valuename']} {conf['value']}")

            RPS_list = []
            TPR_list = []

            # Add the configuration to the lighttpd configuration file
            if not add_config_to_lighttpd_conf(filepath, conf['name'], conf['value']):
                continue

            # Run the benchmark multiple times
            for i in range(1, 2):
                bench_output = run_lighttpd_benchmark(requests, concurrency)
                if bench_output == "error":
                    break
                RPS, TPR = extract_data_from_output(bench_output)
                print(f"{i}th : Requests per second: {RPS}, Time per request: {TPR} ms")

                RPS_list.append(float(RPS))
                TPR_list.append(float(TPR))

            if bench_output == "error":
                results.append([conf['name'], conf['valuename'], conf['value'], 'timeout', 'timeout'])
                continue

            # Calculate average values
            RPS_list = np.array(RPS_list)
            TPR_list = np.array(TPR_list)

            RPS_avg = np.mean(RPS_list) if len(RPS_list) else None
            TPR_avg = np.mean(TPR_list) if len(TPR_list) else None

            print(f"Average RPS: {RPS_avg}, Average TPR: {TPR_avg} ms")


        # Save the results to a CSV file
        with open(results_file, 'w', newline='') as file:
            writer = csv.writer(file)
            writer.writerow(['Name', 'ValueName', 'Value', 'Average RPS', 'Average TPR'])
            writer.writerows(results)

```

### redis-bench
```python
import csv
import subprocess
import numpy as np
import time
import os

# Define workload parameter combinations
workloads = [
    ("get", 10000, 50),
    ("set", 10000, 50),
    ("get", 100000, 100),
    ("set", 100000, 100)
]

# Function to run the Redis benchmark
def run_redis_benchmark(command):
    try:
        result = subprocess.run(command, shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    except Exception as e:
        print(e)
        return "error"
    return result.stdout

# Function to extract throughput and latency from benchmark output
def extract_data_from_output(output):
    lines = output.split('\n')
    tps = []
    avg_latency = []

    for i, line in enumerate(lines):
        if 'throughput summary:' in line:
            parts = line.split()
            tps = float(parts[2])

        if 'latency summary (msec)' in line:
            for j in range(i + 1, len(lines)):
                if 'avg' in lines[j]:
                    avg_latency = float(lines[j + 1].split()[0])
                    break

    return tps, avg_latency

# Function to clear the Redis configuration file
def clear_redis_conf(file_path):
    with open(file_path, "w") as file:
        file.truncate()

# Function to add configuration settings to Redis config file
def add_config_to_redis_conf(file_path, configname, configvalue):
    command = f"echo '{configname} {configvalue}' > /path/to/redis/config/redis.conf"
    command1 = "redis-cli shutdown"

    with open("/dev/null", "w") as devnull:
        subprocess.run(command1, shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
        subprocess.run(command, shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
        subprocess.Popen(["redis-server", "/path/to/redis/config/redis.conf"], stdout=devnull, stderr=devnull)
    time.sleep(2)  # Give Redis 2 seconds to start

    return True

# Function to read a CSV file into a dictionary
def read_csv_to_dict(filename):
    data = []
    with open(filename, mode='r', encoding='utf-8') as file:
        reader = csv.DictReader(file)
        for row in reader:
            data.append(row)
    return data

# Function to calculate the percentage change from the standard value
def calculate_change(current, standard):
    return ((current - standard) / standard) * 100 if standard != 0 else 0


# Main execution block
if __name__ == '__main__':
    # Configuration file path and data source
    filepath = "/path/to/redis/config/redis.conf"
    confdata = read_csv_to_dict("/path/to/config/data.csv")
    tps_stand = 0.375789474
    latency_stand = 0.097052632

    # Loop through the workloads
    for workload_type, num_requests, concurrency in workloads:
        results_file = f"/path/to/results/redisbenchmark-{workload_type}-{num_requests}-{concurrency}.csv"

        # Check if the result file already exists
        if os.path.exists(results_file):
            print(f"Result file for workload {workload_type}-{num_requests}-{concurrency} already exists. Skipping...")
            continue

        print(f"Running workload: {workload_type}, requests: {num_requests}, concurrency: {concurrency}")
        results = []

        with open("/dev/null", "w") as devnull:
            subprocess.Popen(["redis-server", filepath], stdout=devnull, stderr=devnull)

        # Loop through each configuration setting
        for conf in confdata:
            clear_redis_conf(filepath)
            print(f"{conf['name']} {conf['valuename']} {conf['value']}")
            try:
                tps_list = []
                latency_list = []

                if not add_config_to_redis_conf(filepath, conf['name'], conf['value']):
                    continue

                # Run benchmark and extract performance data
                for i in range(1, 2):
                    command = f"redis-benchmark -t {workload_type} -n {num_requests} -c {concurrency}"
                    bench_output = run_redis_benchmark(command)

                    if bench_output == "error":
                        break

                    tpss, latencys = extract_data_from_output(bench_output)

                    print(f"Average TPS: {tpss}, Average Latency: {latencys} ms")

                    results.append([conf['name'], conf['valuename'], conf['value'], tpss, latencys])

            except Exception as e:
                print(f"---error---: {e}")
                results.append([conf['name'], conf['valuename'], conf['value'], 'error', 'error'])
                continue

            # Save the results to a CSV file
            with open(results_file, 'w', newline='') as file:
                writer = csv.writer(file)
                writer.writerow(['Name', 'ValueName', 'Value', 'TPS', 'Latency'])
                writer.writerows(results)
```

### tomcat-ApacheBench
```python
import csv
import subprocess
import numpy as np
import time
import re
import shutil
import os
import xml.etree.ElementTree as ET

# Password for sudo commands (this should be handled securely in real applications)
password = '********'

# Define different workload parameters
workloads = [
    (10000, 1000),
    (1000, 10),
]

def run_tomcat_benchmark(requests, concurrency):
    try:
        command = f"ab -n {requests} -c {concurrency} http://localhost:8080/"
        result = subprocess.run(command, shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, timeout=10)
    except Exception as e:
        print(e)
        return "error"
    return result.stdout

def extract_data_from_output(output):
    requests_per_second_pattern = r"Requests per second:\s+(\d+\.\d+)"
    time_per_request_pattern = r"Time per request:\s+(\d+\.\d+)"

    requests_per_second = re.search(requests_per_second_pattern, output).group(1)
    time_per_request = re.search(time_per_request_pattern, output).group(1)

    return float(requests_per_second), float(time_per_request)

def backup_server_xml(server_xml_path, backup_path):
    shutil.copy(server_xml_path, backup_path)

def restore_server_xml(server_xml_path, backup_path):
    shutil.copy(backup_path, server_xml_path)

def modify_server_xml(server_xml_path, attribute, new_value):
    tree = ET.parse(server_xml_path)
    root = tree.getroot()
    
    connector = root.find('.//Connector')
    
    if connector is not None:
        connector.set(attribute, new_value)

    tree.write(server_xml_path, encoding='utf-8', xml_declaration=True)

def start_tomcat():
    try:
        subprocess.run(f"echo {password} | sudo -S /usr/local/tomcat/bin/startup.sh", shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    except Exception as e:
        print(e)
        return False
    time.sleep(5)  # Allow 5 seconds for Tomcat to start
    return True

def stop_tomcat():
    try:
        subprocess.run(f"echo {password} | sudo -S /usr/local/tomcat/bin/shutdown.sh", shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    except Exception as e:
        print(e)

def read_csv_to_dict(filename):
    data = []
    with open(filename, mode='r', encoding='utf-8') as file:
        reader = csv.DictReader(file)
        for row in reader:
            data.append(row)
    return data

def calculate_change(current, standard):
    return ((current - standard) / standard) * 100 if standard != 0 else 0

if __name__ == '__main__':
    server_xml_path = '/path/to/tomcat/conf/server.xml'  # Path to Tomcat server.xml
    backup_path = '/path/to/backup/server_backup.xml'   # Path for server.xml backup
    confdata = read_csv_to_dict('/path/to/csv/test.csv')  # Path to configuration data CSV file
    RPS_stand = 0
    TPR_stand = 0

    # Backup the original server.xml file
    backup_server_xml(server_xml_path, backup_path)

    for requests, concurrency in workloads:
        results_file = f"/path/to/results/Apachebench-{requests}-{concurrency}.csv"  # Path to save results

        # Check if result file already exists
        if os.path.exists(results_file):
            print(f"Result file for workload {requests}-{concurrency} already exists. Skipping...")
            continue

        print(f"Running workload: {requests} requests, {concurrency} concurrency")
        results = []

        for conf in confdata:
            stop_tomcat()
            restore_server_xml(server_xml_path, backup_path)
            print(f"Modifying {conf['name']} to {conf['value']}")

            RPS_list = []
            TPR_list = []

            modify_server_xml(server_xml_path, conf['name'], conf['value'])

            if not start_tomcat():
                results.append([conf['name'], conf['valuename'], conf['value'], 'error', 'error', 'error', 'error'])
                continue

            for i in range(1, 6):
                bench_output = run_tomcat_benchmark(requests, concurrency)
                if bench_output == "error":
                    break
                RPS, TPR = extract_data_from_output(bench_output)
                print(f"{i}th : Requests per second: {RPS}, Time per request: {TPR} ms")

                RPS_list.append(RPS)
                TPR_list.append(TPR)

            if bench_output == "error":
                results.append([conf['name'], conf['valuename'], conf['value'], 'timeout', 'timeout', 'timeout', 'timeout'])
                continue

            RPS_list = np.array(RPS_list)
            TPR_list = np.array(TPR_list)

            RPS_avg = np.mean(RPS_list) if len(RPS_list) else None
            TPR_avg = np.mean(TPR_list) if len(TPR_list) else None

            print(f"Average RPS: {RPS_avg}, Average TPR: {TPR_avg} ms")

            results.append([conf['name'], conf['valuename'], conf['value'], RPS_avg, TPR_avg])

        # Save results to CSV file
        with open(results_file, 'w', newline='') as file:
            writer = csv.writer(file)
            writer.writerow(['Name', 'ValueName', 'Value', 'Average RPS', 'Average TPR', 'RPS Change', 'TPR Change'])
            writer.writerows(results)

    # Restore original server.xml file
    restore_server_xml(server_xml_path, backup_path)
    print("Original server.xml restored.")
```

### tomcat-JMeter
```python
import csv
import subprocess
import numpy as np
import time
import re
import shutil
import os
import xml.etree.ElementTree as ET

# Replace with your actual password for sudo operations
password = 'your_password_here'

# Define different workload parameter combinations
def run_tomcat_benchmark(id):
    try:
        # Replace with the correct path to your JMeter installation and test files
        command = f"/path/to/jmeter -n -t /path/to/tomcat/{id}.jmx -l /path/to/results.jtl -j /path/to/jmeter.log"
        result = subprocess.run(command, shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, timeout=10)
    except Exception as e:
        print(e)
        return "error"
    return result.stdout

def extract_data_from_output(output):
    # Regex pattern to extract throughput value from JMeter output
    throughput_pattern = r"summary =\s+\d+\s+in\s+\d+:\d+:\d+\s+=\s+([\d.]+)/s"
    throughput = re.search(throughput_pattern, output).group(1)
    return float(throughput)

def backup_server_xml(server_xml_path, backup_path):
    shutil.copy(server_xml_path, backup_path)

def restore_server_xml(server_xml_path, backup_path):
    shutil.copy(backup_path, server_xml_path)

def modify_server_xml(server_xml_path, attribute, new_value):
    tree = ET.parse(server_xml_path)
    root = tree.getroot()
    connector = root.find('.//Connector')
    
    if connector is not None:
        connector.set(attribute, new_value)

    tree.write(server_xml_path, encoding='utf-8', xml_declaration=True)

def start_tomcat():
    try:
        # Replace with your Tomcat startup command and correct password handling
        subprocess.run(f"echo {password} | sudo -S /path/to/tomcat/bin/startup.sh", shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    except Exception as e:
        print(e)
        return False
    time.sleep(5)  # Allow Tomcat 5 seconds to start
    return True

def stop_tomcat():
    try:
        # Replace with your Tomcat shutdown command and correct password handling
        subprocess.run(f"echo {password} | sudo -S /path/to/tomcat/bin/shutdown.sh", shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    except Exception as e:
        print(e)

def read_csv_to_dict(filename):
    data = []
    with open(filename, mode='r', encoding='utf-8') as file:
        reader = csv.DictReader(file)
        for row in reader:
            data.append(row)
    return data

def calculate_change(current, standard):
    return ((current - standard) / standard) * 100 if standard != 0 else 0

if __name__ == '__main__':
    # Replace with the correct path to your server.xml file and backup location
    server_xml_path = '/path/to/tomcat/conf/server.xml'
    backup_path = '/path/to/tomcat/server_backup.xml'

    # Read configuration data from a CSV file (adjust file path as necessary)
    confdata = read_csv_to_dict('/path/to/tomcat/test.csv')
    
    start_tomcat()

    # Backup the original server.xml file
    backup_server_xml(server_xml_path, backup_path)

    for id in range(1, 3):
        # Replace with your actual results file path
        results_file = f"/path/to/tomcat/tomcat-jmeter-{id}.csv"
        
        throughput_list = []
        for i in range(1, 6):
            # Run JMeter benchmark and extract throughput data
            bench_output = run_tomcat_benchmark(id)
            throughput = extract_data_from_output(bench_output)
            throughput_list.append(float(throughput))
        
        throughput_list = np.array(throughput_list)
        throughput_avg = np.mean(throughput_list) if len(throughput_list) else None

        # Check if the result file already exists
        if os.path.exists(results_file):
            print(f"Result file for workload {id} already exists. Skipping...")
            continue

        print(f"Running workload: {id}")
        results = []

        for conf in confdata:
            stop_tomcat()
            restore_server_xml(server_xml_path, backup_path)
            print(f"Modifying {conf['name']} to {conf['value']}")

            modify_server_xml(server_xml_path, conf['name'], conf['value'])

            if not start_tomcat():
                results.append([conf['name'], conf['valuename'], conf['value'], 'error'])
                continue

            bench_output = run_tomcat_benchmark(id)
            if bench_output == "error":
                results.append([conf['name'], conf['valuename'], conf['value'], 'timeout'])
                continue

            throughput = extract_data_from_output(bench_output)
            print(f"throughput: {throughput}")

            results.append([conf['name'], conf['valuename'], conf['value'], throughput])

        # Save results to CSV file
        with open(results_file, 'w', newline='') as file:
            writer = csv.writer(file)
            writer.writerow(['Name', 'ValueName', 'Value', 'throughput'])
            writer.writerows(results)

    # Restore the original server.xml file
    restore_server_xml(server_xml_path, backup_path)
    print("Original server.xml restored.")
```

### Hadoop-Hibench
```python
import csv
import subprocess
import numpy as np
import time
import re
import xml.etree.ElementTree as ET
from datetime import datetime

# Replace with your actual password for sudo operations
password = 'your_password_here'

def read_csv_to_dict(filename):
    data = []
    with open(filename, mode='r', encoding='utf-8') as file:
        reader = csv.DictReader(file)
        for row in reader:
            # Convert each row into a dictionary (column headers as keys)
            data.append(row)
    return data

def run_hibench():
    # Run the Hadoop performance test command
    try:
        # Replace with your actual Hadoop HiBench path
        subprocess.run(f"echo {password} | sudo -S /path/to/HiBench/bin/run_all.sh", shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    except Exception as e:
        print(e)

def clear_hadoop_conf(file_path):
    tree = ET.parse(file_path)
    root = tree.getroot()
    for prop in root.findall('property'):
        test = prop.find('test')
        if test is not None:
            root.remove(prop)
    tree.write(file_path, encoding='UTF-8', xml_declaration=True)

def startsys():
    # Replace with the correct command to start your Hadoop system
    commandstart = f"/path/to/hadoop/sbin/start-all.sh"

    try:
        subprocess.run(commandstart, shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
        print("Hadoop services have been started successfully.")
    except Exception as e:
        print(e)

def stopsys():
    # Replace with the correct command to stop your Hadoop system
    commandstop = f"/path/to/hadoop/sbin/stop-all.sh"

    try:
        subprocess.run(commandstop, shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
        print("Hadoop services have been stopped successfully.")
    except Exception as e:
        print(e)

def add_config_to_hadoop_conf(file_path, configname, configvalue):
    stopsys()

    tree = ET.parse(file_path)
    root = tree.getroot()

    # Create new <property> element and its child elements
    new_property = ET.SubElement(root, 'property')
    name_element = ET.SubElement(new_property, 'name')
    name_element.text = configname
    value_element = ET.SubElement(new_property, 'value')
    value_element.text = configvalue
    test_element = ET.SubElement(new_property, 'test')
    test_element.text = "test"
    
    # Save the modified XML file
    tree.write(file_path, encoding='UTF-8', xml_declaration=True)

    startsys()

    return True

if __name__ == '__main__':
    # Dictionary to map software to their respective config file paths
    xmlfile = {
        'hdfs': 'hdfs-site.xml',
        'mapreduce': 'mapred-site.xml',
        'yarn': 'yarn-site.xml'
    }

    # Specify the software you want to test
    software = "mapreduce or yarn or hdfs"
    
    # Construct the file path using the software variable
    filepath = f'/path/to/hadoop/etc/hadoop/{xmlfile[software]}'

    # Start Hadoop services
    startsys()

    # Read configuration data from a CSV file (adjust file path as necessary)
    confdata = read_csv_to_dict(f"/path/to/HadoopGT/{software}test2.csv")

    for conf in confdata:
        # Clear existing configuration from the Hadoop config file
        clear_hadoop_conf(filepath)

        # Log the configuration change to a report file
        with open("/path/to/HiBench/report/hibench.report", 'a', encoding='utf-8') as file:
            file.write(f"{conf['name']}|{conf['valuename']}|{conf['value']}\n")
        print(f"{conf['name']}|{conf['valuename']}|{conf['value']}")

        # Add the new configuration to the Hadoop config file
        # add_config_to_hadoop_conf(filepath, conf['name'], conf['value'])

        # Run the HiBench benchmark
        run_hibench()

```

### Hbase-YCSB
```python
import csv
import subprocess
import xml.etree.ElementTree as ET
import re

# Replace with your actual password for sudo operations
password = 'your_password_here'

def read_csv_to_dict(filename):
    data = []
    with open(filename, mode='r', encoding='utf-8') as file:
        reader = csv.DictReader(file)
        for row in reader:
            data.append(row)
    return data

def run_command(command, timeout=None):
    try:
        result = subprocess.run(command, shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, timeout=timeout)
        print(result.stdout)
        return result.stdout
    except subprocess.CalledProcessError as e:
        print(f"Error executing command: {command}\n{e.output}")
        return None
    except subprocess.TimeoutExpired as e:
        print(f"Command timed out: {command}\n{e.output}")
        return None

def create_hbase_table():
    commands = '''
    # create your table
    '''
    process = subprocess.Popen(['hbase', 'shell'], stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    try:
        stdout, stderr = process.communicate(commands, timeout=20)
        print(stdout)
        if process.returncode != 0:
            print(f"Error creating HBase table: {stderr}")
    except subprocess.TimeoutExpired:
        process.kill()
        stdout, stderr = process.communicate()
        print(f"Command timed out while creating HBase table: {stderr}")

def load_data():
    # Replace with your YCSB home directory path
    command = f"{YCSB_HOME}/bin/ycsb load hbase20 -cp /etc/hbase/conf/ -p columnfamily=cf -P {YCSB_HOME}/workloads/workload"
    run_command(command, timeout=20)

def run_ycsb():
    # Replace with your YCSB home directory path
    command = f"{YCSB_HOME}/bin/ycsb run hbase20 -cp /etc/hbase/conf/ -p columnfamily=cf -p recordcount= -p operationcount= -P {YCSB_HOME}/workloads/workload -threads "
    try:
        result = subprocess.run(command, shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, timeout=20)
        print("Command executed successfully:")
        runtime, throughput = parse_and_store_output(result.stdout)
        print(runtime, throughput)
        return runtime, throughput
    except subprocess.CalledProcessError as e:
        print(f"Error executing command: {command}")
        print(f"Error output: {e.stderr}")
        return "0", "0"
    except subprocess.TimeoutExpired as e:
        print(f"Command timed out: {command}")
        return "0", "0"

def parse_and_store_output(output):
    runtime_pattern = r'\[OVERALL\], RunTime\(ms\), (\d+)'
    throughput_pattern = r'\[OVERALL\], Throughput\(ops/sec\), ([\d.]+)'

    runtime_match = re.search(runtime_pattern, output)
    throughput_match = re.search(throughput_pattern, output)

    if runtime_match and throughput_match:
        runtime = runtime_match.group(1)
        throughput = throughput_match.group(1)
        return runtime, throughput
    else:
        return "0", "0"

def clear_hadoop_conf(file_path):
    tree = ET.parse(file_path)
    root = tree.getroot()
    for prop in root.findall('property'):
        test = prop.find('test')
        if test is not None:
            root.remove(prop)
    tree.write(file_path, encoding='UTF-8', xml_declaration=True)

def start_hbase():
    # Replace with the correct command to start HBase
    command = "/path/to/hbase/bin/start-hbase.sh"
    run_command(command, timeout=20)

def stop_hbase():
    # Replace with the correct command to stop HBase
    command1 = "hbase-daemon.sh stop master"
    command2 = "/path/to/hbase/bin/stop-hbase.sh"
    run_command(command1, timeout=10)
    run_command(command2, timeout=20)

def add_config_to_hadoop_conf(file_path, configname, configvalue):
    stop_hbase()
    tree = ET.parse(file_path)
    root = tree.getroot()
    new_property = ET.SubElement(root, 'property')
    name_element = ET.SubElement(new_property, 'name')
    name_element.text = configname
    value_element = ET.SubElement(new_property, 'value')
    value_element.text = configvalue
    test_element = ET.SubElement(new_property, 'test')
    test_element.text = "test"
    tree.write(file_path, encoding='UTF-8', xml_declaration=True)
    start_hbase()
    return True

def write_results_to_csv(results):
    # Write results to CSV file (adjust path as needed)
    with open("/path/to/result.csv", 'w', encoding='utf-8', newline='') as csvfile:
        writer = csv.writer(csvfile, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)
        
        writer.writerow(['conf_name', 'conf_valuename', 'conf_value', 'runtime', 'throughput'])

        # Write each configuration and result as a row
        for row in results:
            writer.writerow(row)

if __name__ == '__main__':
    # Replace with your actual YCSB home directory path
    YCSB_HOME = "/path/to/ycsb"
    # Replace with your actual Hadoop config file path
    filepath = '/path/to/hbase/conf/hbase-site.xml'
    confdata = read_csv_to_dict("/path/to/HadoopGT/hbasetests/test.csv")

    # Store all results
    results = []
    start_hbase()
    for conf in confdata:
        clear_hadoop_conf(filepath)
        print(f"Configuring: {conf['name']}|{conf['valuename']}|{conf['value']}")
        add_config_to_hadoop_conf(filepath, conf['name'], conf['value'])

        # Create HBase table
        try:
            create_hbase_table()    

            # Load data
            load_data()

        except:
            results.append([f"{conf['name']}|{conf['valuename']}|{conf['value']}", "error", "error"])
            continue

        # Run YCSB test and get results
        runtime, throughput = run_ycsb()

        # Store the configuration and test results in one row
        results.append([f"{conf['name']}|{conf['valuename']}|{conf['value']}", runtime, throughput])

        # Write all results to CSV file
        write_results_to_csv(results)

        print("YCSB test results have been successfully written to CSV.")

```