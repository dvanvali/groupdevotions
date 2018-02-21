module.exports = function(grunt) {
  var config = {
    src: 'src/main/webapp/',
    dest: 'target/groupdevotions-0.0.1-SNAPSHOT/'
  };

  var nowInMilliseconds = function () {
      return (new Date()).getTime();
  };

  // Project configuration.
  grunt.initConfig({
    config: config,
    pkg: grunt.file.readJSON('package.json'),
    watch: {
      options: {
        livereload: true
      },
      scripts: {
        files: ['<%= config.src %>*.js','<%= config.src %>**/*.js'],
        tasks: ['concat:app','replace:html'],
        options: {
          spawn: false
        }
      },
      html: {
        files: ['<%= config.src %>**/*.html'],
        tasks: ['copy:html','replace:html','html2js'],
        options: {
          spawn: false
        }
      },
      css: {
        files: ['<%= config.src %>**/*.less','<%= config.src %>*.less'],
        tasks: ['concat:less','less:app','clean:finish','replace:html'],
        options: {
          spawn: false
        }
      }
    },
    replace: {
        html: {
            options: {
                patterns: [
                    {
                      match: 'version',
                      replacement: nowInMilliseconds
                    }
                ]
            },
            files: [
                {
                  cwd: config.src,
                  expand: true,
                  src: ['index.html'],
                  dest: config.dest
                }
            ]
        }
    },
    concat: {
        app : {
          /*
          options: {
            banner: "\"use strict\";\n\n",
            tripsBanners: true,
            separator: "\n\n"
          },
          */
          src: [
            config.src + "app.js",
            config.src + "**/*.js"
          ],
          dest: config.dest + 'scripts/app.js'
        },
        libraries : {
          src: [
            'bower_components/html5-boilerplate/js/vendor/modernizr-2.6.2.min.js',
            'bower_components/jquery/dist/jquery.js',
            'bower_components/bootstrap/dist/js/bootstrap.js',
            'bower_components/underscore/underscore.js',
            'bower_components/angular/angular.min.js',
            'bower_components/angular-sanitize/angular-sanitize.min.js',
            'bower_components/angular-route/angular-route.min.js',
            'bower_components/angular-resource/angular-resource.min.js',
            'bower_components/angular-touch/angular-touch.min.js',
            'bower_components/angular-bootstrap/ui-bootstrap-tpls.min.js',
            'bower_components/linkifyjs/linkify.min.js',
            'bower_components/linkifyjs/linkify-string.min.js'
          ],
          dest: config.dest + 'scripts/libraries.min.js'
        },
        less : {
          src: [
            config.src + '_*.less',
            config.src + 'app.less',
            config.src + '**/*.less'
          ],
          dest: config.dest + 'temp/app.less'
        }
    },
    copy: {
      html: {
        files: [
          {
            expand: true,
            cwd: config.src,
            src: ['**/*.html'],
            dest: config.dest,
            filter: 'isFile'
          }
        ]
      },
      maincss: {
        files: [
          {
            expand: true,
            flatten: true,
            cwd: './',
            src: [
              'bower_components/**/bootstrap.css',
              'bower_components/**/main.css',
              'bower_components/**/normalize.css'
            ],
            dest: config.dest + 'css/',
            filter: 'isFile'
          },
          {
            expand: true,
            flatten: true,
            cwd: './',
            src: ['bower_components/bootstrap/fonts/*'],
            dest: config.dest + 'fonts/',
            filter: 'isFile'
          }
        ]
      }
    },

    less: {
      app: {
        files: {
          "<%= config.dest + 'css/app.css' %>": config.dest + "/temp/app.less"
        }
      }
    },

    html2js: {
      options: {
        base: "src/main/webapp",
        module: 'app-templates',
        rename: function (moduleName) {
          return moduleName.replace(config.dest, '/');
        }
      },
      main: {
        src: [
            config.src + 'components/blog/*.html',
            config.src + 'components/devotion/*.html',
            config.src + 'components/journal/*.html',
            config.src + 'partials/desktop.html',
            config.src + 'components/home/home.html',
            config.src + 'components/login/login.html'
        ],
        dest: config.dest + 'scripts/templates.js'
      }
    },

    clean: {
      start: {
        src: [ config.dest + "temp"]
      },
      finish: {
        src: [ config.dest + "temp"]
      }
    },

    protractor: {
      options: {
        configFile: 'protractor-conf.js'
        // debugger; stops inside node.  Use repl to get into the context of the debugger.
        // ctrl + c to exit repl
        // c to continue execution (kills batch in windows...)
        //, debug: true
      }
      ,dev: {
        options: {
          args: {
            baseUrl: 'http://localhost:8080/'
          }
        }
      }
    }
  });

  // Load the plugin that provides the "uglify" task.
  grunt.loadNpmTasks('grunt-contrib-watch');
  grunt.loadNpmTasks('grunt-contrib-copy');
  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-contrib-less');
  grunt.loadNpmTasks('grunt-contrib-clean');
  grunt.loadNpmTasks('grunt-html2js');
  grunt.loadNpmTasks('grunt-replace');
  grunt.loadNpmTasks('grunt-protractor-runner');

  // Default task(s).
  grunt.registerTask('default', ['build']);
  grunt.registerTask('build', ['clean:start', 'copy:html', 'replace:html', 'concat:app', 'concat:libraries', 'copy:maincss', 'concat:less', 'less:app', 'html2js', 'clean:finish']);

};