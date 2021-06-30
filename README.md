# A collection of xjc plugins






use with xjc generators like this  
```
  <plugins>
            <plugin>
                <groupId>org.jvnet.jaxb2.maven2</groupId>
                <artifactId>maven-jaxb2-plugin</artifactId>
                <executions>
                    <execution>
                        <goals>
                            <goal>generate</goal>
                        </goals>
                        <phase>generate-test-sources</phase>
                    </execution>
                </executions>
                <configuration>
                    <verbose>true</verbose>
                    <extension>true</extension>
                    <plugins>
                        <!--
                        <plugin>
                            <groupId>dk.conspicio</groupId>
                            <artifactId>xew</artifactId>
                            <version>1.4</version>
                        </plugin>
                            -->
                        <plugin>
                            <groupId>${project.groupId}</groupId>
                            <artifactId>xjc-validation</artifactId>
                            <version>${project.version}</version>
                        </plugin>
                        <plugin>
                            <groupId>${project.groupId}</groupId>
                            <artifactId>xjc-tostring</artifactId>
                            <version>${project.version}</version>
                        </plugin>
                        <plugin>
                            <groupId>${project.groupId}</groupId>
                            <artifactId>xjc-transient</artifactId>
                            <version>${project.version}</version>
                        </plugin>
                    </plugins>
                    <args>
                        <arg>-Xvalidation</arg>
                        <arg>-Xtransient</arg>
                        <arg>-Xtostring</arg>
                        <arg>-nv</arg>
                    </args>
                    <!-- Changes the default schema directory -->
                    <schemaDirectory>src/test/resources</schemaDirectory>
                    <schemaIncludes>
                        <include>rss-2_0.xsd</include>
                    </schemaIncludes>
                    <bindingDirectory>src/test/resources</bindingDirectory>
                    <bindingIncludes>
                        <include>bindings-xjb.xml</include>
                    </bindingIncludes>
                </configuration>
                <!--
                <dependencies>
                    <dependency>
                        <groupId>biz.mofokom</groupId>
                        <artifactId>xjc-debug</artifactId>
                        <version>${pom.version}</version>
                    </dependency>
                </dependencies>
                -->
            </plugin>
        </plugins>
```

Or 

```
<plugins>
            <plugin>
                <groupId>org.apache.cxf</groupId>
                <artifactId>cxf-codegen-plugin</artifactId>
                <version>${cxf.version}</version>
                <executions>
                    <execution>
                        <phase>generate-test-sources</phase>
                        <configuration>
                            <wsdlOptions>
                                <wsdlOption>
                                    <wsdl>${basedir}/src/test/resources/service.wsdl</wsdl>
                                    <extraargs>
              <!--
            <extraarg>-xjc-b,binding.xjb</extraarg>
            <extraarg>-xjc-Xaddressing</extraarg>
            -->
                                        <extraarg>-xjc-Xdebug</extraarg>
                                    </extraargs> 
                                </wsdlOption>
                            </wsdlOptions>
                        </configuration>
                        <goals>
                            <goal>wsdl2java</goal>
                        </goals>
                    </execution>
                </executions>
                <dependencies>
                    <!--
                    <dependency>
                        <groupId>org.apache.cxf</groupId>
                        <artifactId>xjc-jaxws-addressing</artifactId>
                        <version>${pom.version}</version>
                    </dependency>
                    -->
                    <dependency>
                        <groupId>${project.groupId}</groupId>
                        <artifactId>xjc-debug</artifactId>
                        <version>${project.version}</version>
                    </dependency>
                </dependencies>
            </plugin>
        </plugins>

```
