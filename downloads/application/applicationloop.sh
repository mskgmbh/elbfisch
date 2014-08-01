#!/bin/sh
# PROJECT   : com.msk.atlas.schwerpunktbestimmung
# MODULE    : applicationloop.sh
# VERSION   : $Revision$
# DATE      : $Date$
# PURPOSE   : <???>
# AUTHOR    : Bernd Schuster, MSK Gesellschaft fuer Automatisierung mbH, Schenefeld
# REMARKS   : -
# CHANGES   : CH#n <Kuerzel> <datum> <Beschreibung>
# LOG       : $Log$

EXITCODE_APPLICATION_NORMAL=0
EXITCODE_APPLICATION_ERROR=1
EXITCODE_APPLICATION_RESTART=40
EXITCODE_SYSTEM_HALT=50
EXITCODE_SYSTEM_RESTART=60

#kill all eventually running old instances
killall java >/dev/null 2>&1
# remove  old locks
rm /var/lock/LCK..ttyS0  > /dev/null 2>&1
cd /usr/local/application

finished=0;
while [ $finished -eq 0 ]; do
   java -Xintgc -cp "./lib/*:./cfg/" raspberry.LogicalTest
   rc=$?
   echo "exit code = $rc"
   case $rc in
        $EXITCODE_APPLICATION_NORMAL)
             echo "application ended normally"
             finished=1
             ;;
        $EXITCODE_APPLICATION_ERROR)
             echo "application ended with error"
             #let the application loop restart the application
             ;;
        $EXITCODE_APPLICATION_RESTART) 
             echo "application is to be restarted"
             #let the application loop restart the application
             ;;
        $EXITCODE_SYSTEM_HALT)         
             echo "system is to be halted"
             shutdown -h now
             ;;             
        $EXITCODE_SYSTEM_RESTART)      
             echo "system is to be restarted"
             shutdown -r now
             ;;
   esac
   finished=1 #do never restart the application
done
echo "script ended"

     
