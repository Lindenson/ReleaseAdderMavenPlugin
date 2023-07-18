# Spring Configuration Properties Versioning Maven Plugin


## The problem
SpringBoot can automatically generate project properties documentation files
[spring-configuration-metadata.json](https://docs.spring.io/spring-boot/docs/current/reference/html/configuration-metadata.html)
and a developer can manually add more.
The question remains open: who automatically traces the differencies in properties from release to release (from git branch to git branch).
If the names of the branches are following some pattern to trace back the release history (e.g. release-X.X.X.X) then it is possible:
- sort the branches by name
- find the current and previous release branches
- get these metadata files from them
- compare them and find which properties were added or removed while moving from the previouse release to the new one
- generate a report

## Plugin creates a report in form of ADOC (Example)

<div id="content" style="background: #2C3E50">
<div class="sect1">
<h4 id="_created_for_release_release_3_3_0">Created for release-3.3.0</h4>
<div class="sectionbody">

</div>
</div>
<div class="sect1">
<h4 id="_properties_added">Properties added</h4>
<div class="sectionbody">
<div class="ulist">
<ul>
<li>
<p>default-proactive-action</p>
<div class="ulist">
<ul>
<li>
<p>input</p>
</li>
<li>
<p>method-name</p>
</li>
<li>
<p>result-variable</p>
</li>
</ul>
</div>
</li>
<li>
<p>lock</p>
<div class="ulist">
<ul>
<li>
<p>cache</p>
<div class="ulist">
<ul>
<li>
<p>enabled</p>
</li>
<li>
<p>size</p>
</li>
</ul>
</div>
</li>
</ul>
</div>
</li>
<li>
<p>token-cache-config</p>
<div class="ulist">
<ul>
<li>
<p>enabled</p>
</li>
<li>
<p>max-size3</p>
</li>
<li>
<p>refresh-ahead-seconds</p>
</li>
</ul>
</div>
</li>
</ul>
</div>
</div>
</div>
<div class="sect1">
	<h4 id="_properties_removed">Properties removed</h4>
<div class="sectionbody">
<div class="ulist">
<ul>
<li>
<p>automatic-language-detection</p>
<div class="ulist">
<ul>
<li>
<p>enabled</p>
</li>
<li>
<p>expected-language</p>
</li>
<li>
<p>foreign-language-detected-keyword</p>
</li>
<li>
<p>gtp-target</p>
</li>
<li>
<p>provider</p>
</li>
<li>
<p>result-variable</p>
</li>
</ul>
</div>
</li>
<li>
<p>callback</p>
<div class="ulist">
<ul>
<li>
<p>allowed-http-destinations</p>
</li>
</ul>
</div>
</li>
</ul>
</div>
</div>
</div>
</div>

## Internals
- plugin works on `install` phase
- the goal is `add_version`
- the *folder* (default `properties_history`) will contain (plugin populates it) all *spring-configuration-metadata.json files of a progect
- this `folder` is to be added, committed and observed by git (important for plugin to work)
- on `install` phase the plugin finds all current *spring-configuration-metadata.json from currently generating JAR and put them into this *folder*
- you could manually add *spring-configuration-metadata.json to this *folder* for the previouse release and `git commit` it to make this plugin start working
- then this `folder` (committed) becomes a history of your properties
- the branch name of release should follow a pattern (to distinguish it from other or technical branches)
- default pattern is `release-(?<number>(\d+\.*)+)[^\d\.]*.*`, named capture is mandatory and corresponds to a number part of a release name
- plugin finds differences between current *metadata.json from JAR and of the previous release (extracts previous files from the last commit of the previous release form `folder`) 
- plugin reports in the form of adoc using a template (can be modified) 
- in order to find the latest release branches names are sorted mathematically using the `number` named capture
- for instance:
<table>
<tbody>
<tr>
<td><b>found branches</b></td>
<td><b>become a numbers</b></td>
<td><b>possible problems</b></td>
</tr>
<tr>
<td><p>release-1</p></td>
<td><p>100</p></td>
<td><p></p></td>
</tr>
<tr>
<td><p>release-1-betta</p></td>
<td><p>100</p></td>
<td><p>collides with the previous - ignored - ERROR in a log suggests to use more specific regex</p></td>
</tr>
<tr>
<td><p>release-1.2</p></td>
<td><p>120</p></td>
<td><p></p></td>
</tr>
<tr>
<td><p>release-1.2.1-alpha101</p></td>
<td><p>121</p></td>
<td><p></p></td>
</tr>
</tbody>
</table> 
  
## Usage example (for typical release-X.X.X.X where X is an integer):
			<plugin>
				<groupId>com.wol</groupId>
				<artifactId>release-adder-maven-plugin</artifactId>
				<version>1.0</version>
				<configuration>
					<folder>release_adder</folder>
					<regex><![CDATA[release-(?<number>(\d+\.*)+)[^\d\.]*.*]]></regex>
				</configuration>
				<executions>
					<execution>
						<phase>install</phase>
						<goals>
							<goal>add_version</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
