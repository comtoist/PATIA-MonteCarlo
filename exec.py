import os
import sys
import time
import multiprocessing
import subprocess
import signal
from os import getpid

if __name__ == '__main__':
    names = sys.argv
    
    for i in range(1,len(names)):
        name = names[i]
        print(name)
        if os.path.isfile(name+".txt"):#supprime le fichier nom_domaine.txt s'il existe deja (depots.txt etc)
            os.remove(name+".txt")
        
        f = open(name+".txt", "w")#crée le fichier nom_domaine.txt contenant le résultat des expés 

        path =os.getcwd()+"/pddl/"+name+"/"
        for file in sorted(os.listdir(path)):
            if file != "domain.pddl":
                print(file)
                domain = path+"domain.pddl"
                file2 = path+file

                p = subprocess.Popen(["java","-cp","classes:lib/pddl4j-4.0.0.jar", "fr.uga.pddl4j.examples.asp.ASP", domain,file2, "-e", "FAST_FORWARD", "-w", "1.2", "-t", "300"],stdout=f,shell=False)
                try:
                    p.wait(300)#timeout
                except:
                    print("timeout")
                    p.kill()
                    os.system("echo "+file+" timed out temps = 5 minutes >> "+name+".txt")
                    pass

                
