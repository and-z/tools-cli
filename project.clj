(defproject it.zimpel/tools-cli "0.1.0-SNAPSHOT"
  :description "Reduce boilerplate for CLI implementation."
  :url ""
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}

  :dependencies [[org.clojure/clojure "1.11.1" :scope "provided"]
                 [org.clojure/tools.cli "1.0.206" :scope "provided"]]

  :target-path "target/%s"

  :profiles
  {:dev
   {:plugins [[lein-codox "0.10.8"]]
    :codox
    {;; exclude internal namespaces from generated documentation
     :namespaces [#"^(?![dev|user])"]}
    :source-paths ["dev-src"]
    :global-vars {*warn-on-reflection* false}}

   :test {:pedantic? :abort
          :global-vars {*warn-on-reflection* true}}})
