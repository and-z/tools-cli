(ns it.zimpel.tools.cli.core-test
  (:require [it.zimpel.tools.cli.core :as sut]
            [clojure.string :as string]
            [clojure.test :as t]))

(defn contains-errors? [s]
  (some? (re-find #"(?i)error" s)))

(def cli-spec [["-d" "--db-uri URI"     "(required) Datomic Database URI"]
               ["-l" "--api-url URL"    "(required) Endpoint URL"]
               ["-p" "--port PORT"      "(required) HTTP Port number" :parse-fn #(Integer/parseInt %)]
               ["-s" "--page-size SIZE" "Number of elements in a Response page" :parse-fn #(Integer/parseInt %)]
               ["-h" "--help"           "Print this help"]])

(defn silent
  "Suppresses printing to the console"
  [m]
  (assoc m :print-summary? false))

(t/deftest print-help
  (t/testing "help should print and return specific error"
    (let [console-output (with-out-str
                           (sut/get-valid-opts '("-h") {:cli-spec cli-spec}))]
      (t/is (and (not (string/blank? console-output))
                 (not (contains-errors? console-output)))
            "Expect the usage description to be printed out (by default) without errors"))

    (let [console-output (with-out-str
                           (sut/get-valid-opts '("-h") (silent {:cli-spec cli-spec})))]
      (t/is (string/blank? console-output)
            "Expect the console output to be suppressed by `:print-summary?` option"))

    (let [{:keys [show-help? summary db-uri]}
          (sut/get-valid-opts ["-d" "some-d" "-h"] (silent {:cli-spec cli-spec}))]

      (t/is (true? show-help?))
      (t/is (not (string/blank? summary)))
      (t/is (= "some-d" db-uri) "Expect a result despite the output suppressing"))))

(t/deftest parse-known-args
  (t/testing "parse single required arg"
    (t/is (= {:api-url "some-string"}
             (sut/get-valid-opts '("-l" "some-string") {:cli-spec cli-spec :required #{:api-url}}))
          "Expect the parsed short key value under the long key")
    (t/is (= {:db-uri "another-string"}
             (sut/get-valid-opts '("--db-uri" "another-string") {:cli-spec cli-spec :required #{:db-uri}}))
          "Parse the long key value also")

    (t/is (= {:db-uri "another-string" :page-size 33}
             (sut/get-valid-opts '("--db-uri" "another-string" "-s" "33") {:cli-spec cli-spec :required #{}}))
          "Parse args if none is required")

    (t/is (= {:db-uri "some-uri"
              :api-url "some-api-url"
              :port 123
              :page-size 100}
             (sut/get-valid-opts '("-d" "some-uri" "--api-url" "some-api-url" "-p" "123" "-s" "100")
                                 {:cli-spec cli-spec :required #{:db-uri :api-url}}))
          "Expect all provided options to be parsed")))

(t/deftest missing-required-args
  (t/testing "expect hints when some required arguments are missing"
    (let [{:keys [error missing-args summary]}
          (sut/get-valid-opts '("-l" "some-string") (silent {:cli-spec cli-spec :required #{:db-uri}}))]
      (t/is (= :missing-required-args error))
      (t/is (= #{:db-uri} missing-args))
      (t/is (not (contains-errors? summary))))

    (let [{:keys [error missing-args summary]}
          (sut/get-valid-opts '("-d" "some-url") (silent {:cli-spec cli-spec :required #{:db-uri :size}}))]
      (t/is (= :missing-required-args error))
      (t/is (= #{:size} missing-args) "Enforce all required args")
      (t/is (not (contains-errors? summary))))))

(t/deftest unknown-arg
  (let [console-output
        (with-out-str
          (sut/get-valid-opts '("-l" "some-string" "-x" "unknown-arg") {:cli-spec cli-spec :required #{:api-url}}))]
    (t/is (contains-errors? console-output)
          "Aborts on unknown args"))

  (let [result (sut/get-valid-opts '("-l" "some-string" "-x" "unknown-arg" "-m" "second-unknown-arg")
                                   (silent {:cli-spec cli-spec :required #{:api-url}}))]
    (t/is (= {:error :invalid-args}
             (select-keys result [:error]))
          "Expect the validation error when receiving unknown arguments")
    (t/is (= 2 (count (:input-errors result))))))

