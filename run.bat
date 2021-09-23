::
::  Copyright (c) 2021 CyberArk Software Ltd. All rights reserved.
:: 
::  Licensed under the Apache License, Version 2.0 (the "License");
::  you may not use this file except in compliance with the License.
::  You may obtain a copy of the License at
:: 
::  http://www.apache.org/licenses/LICENSE-2.0
:: 
::  Unless required by applicable law or agreed to in writing, software
::  distributed under the License is distributed on an "AS IS" BASIS,
::  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
::  See the License for the specific language governing permissions and
::  limitations under the License.
:: 

:: To spin up the angular project, run `ng serve` command under angular folder
START "angular serve" cmd /c "cd %~dp0\angular && ng serve"

:: To spin up the tomcat server, run `mvn spring-boot:run` command under spring-boot folder
START "spring boot run" cmd /c "cd %~dp0\spring-boot && mvn spring-boot:run"