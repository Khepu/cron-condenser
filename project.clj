(defproject cron-condenser "1.0.0"
  :description "A tool to reduce a list of cron expressions down to the minimum expressions needed to describe the same intervals as the original list."
  :license {:name "EPL-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/tools.cli "1.0.206"]
                 [org.clojure/spec.alpha "0.3.218"]

                 [macroz/tangle "0.2.2"]]
  :plugins [[lein-cloverage "1.2.2"]]
  :main ^:skip-aot cron-condenser.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
