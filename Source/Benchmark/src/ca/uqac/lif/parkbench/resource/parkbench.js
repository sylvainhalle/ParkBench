/*
    ParkBench, a versatile benchmark environment
    Copyright (C) 2015 Sylvain Hallé

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 *  The minimum interval for refreshing the list of tests (in ms)
 */
var MIN_REFRESH_INTERVAL = 1000;

/**
 *  The interval for refreshing the list of tests (in ms)
 */
var REFRESH_INTERVAL = 2000;

/**
 *  The interval for refreshing the graphs (in multiples
 *  of the test refresh interval)
 */
var PLOT_REFRESH_MULTIPLIER = 2;

/**
 *  The number of times the list of test has been refreshed 
 */
var REFRESH_COUNT = 0;


function refresh_test_list(incremental) {
  $.ajax({
    url         : "/status",
    contentType : "application/json",
    success     : function(result) {
      // Slow down refresh interval according to size of server response
      // The problem is not the server, it's the browser that can't keep up
      REFRESH_INTERVAL = Math.max(MIN_REFRESH_INTERVAL, result.tests.length * 10);
      REFRESH_COUNT++;
      $("#refresh-interval").html(Math.round(REFRESH_INTERVAL / 1000));
      // Refresh the summary table
      if (result.status["status-done"])
        $("#status-nb-done").html(result.status["status-done"]);
      else
        $("#status-nb-done").html("0");
      if (result.status["status-not-ready"])
        $("#status-nb-not-ready").html(result.status["status-not-ready"]);
      else
        $("#status-nb-not-ready").html("0");
      if (result.status["status-prerequisites"])
          $("#status-nb-prerequisites").html(result.status["status-prerequisites"]);
        else
          $("#status-nb-prerequisites").html("0");
      if (result.status["status-impossible"])
          $("#status-nb-impossible").html(result.status["status-impossible"]);
        else
          $("#status-nb-impossible").html("0");
      if (result.status["status-ready"])
        $("#status-nb-ready").html(result.status["status-ready"]);
      else
        $("#status-nb-ready").html("0");
      if (result.status["status-running"])
        $("#status-nb-running").html(result.status["status-running"]);
      else
        $("#status-nb-running").html("0");
      if (result.status["status-failed"])
        $("#status-nb-failed").html(result.status["status-failed"]);
      else
        $("#status-nb-failed").html("0");
      if (result.status["status-queued"])
        $("#status-nb-queued").html(result.status["status-queued"]);
      else
        $("#status-nb-queued").html("0");
      if (!incremental)
      {
    	  // Create table contents
    	  $("#benchmark-name").html(result.name);
	      var out_list = "";
	      out_list += "<thead><tr><th></th><th>Status</th><th>Duration</th><th>Name</th>";
	      for (var j = 0; j < result["param-names"].length; j++) {
	        var param_name = result["param-names"][j];
	        out_list += "<th>" + param_name + "</th>";
	      }
	      out_list += "<th></th></tr></thead>\n";
	      out_list += "<tbody>\n";
	      for (var i = 0; i < result.tests.length; i++) {
	        var test = result.tests[i];
	        var class_grey = "";
	        if (test["can-run"] === "false") {
	        	class_grey = " class=\"impossible\" ";
	        }
	        out_list += "<tr " + class_grey + "id=\"tr-test-" + test.id + "\">\n";
	        out_list += fill_table_line(test, result["param-names"]);
	        out_list += "</tr>\n";
	      }
	      out_list += "</tbody>\n";
	      $("#test-list").html(out_list);
	      $("#test-list").tablesorter();
	      // Create plot contents
	      create_plots(result["plots"]);
	  }
	  else
	  {
	    for (var i = 0; i < result.tests.length; i++) {
	      var test = result.tests[i];
	      var element = $("#tr-test-" + test.id);
	      var checked = $("#chktest-" + test.id).prop("checked");
          element.html(fill_table_line(test, result["param-names"]));
          if (checked)
          	$("#chktest-" + test.id).prop("checked", true);
	    }
        // Refresh plot contents
	    if (REFRESH_COUNT % PLOT_REFRESH_MULTIPLIER === 0) {
	    	for (var i = 0; i < result["plots"].length; i++) {
	    		var plot_id = result["plots"][i];
	    		refresh_plot(plot_id);
	    	}
	    }
	  }
  }});
};

function fill_table_line(test, param_names) {
    var out_list = "";
    out_list += "<td><input type=\"checkbox\" class=\"chk-test\" id=\"chktest-" + test.id + "\" /></td>\n";
    out_list += "<td>" + get_status_div(test.status, test.prerequisites, test["can-run"], test.id) + "</td>\n";
    out_list += "<td>" + get_time_string(test.status, test.starttime, test.endtime) + "</td>\n";
    //out_list += "<td>" + test.id + "</td>\n";
    out_list += "<td>" + test.name + "</td>\n";
    for (var j = 0; j < param_names.length; j++) {
      var param_name = param_names[j];
      out_list += "<td>" + test.input[param_name] + "</td>";
    }
    out_list += "<td>";
    if (test["can-run"] === "true") {
        out_list += "<button class=\"btn btn-mini\" onclick=\"start_test(" + test.id + ");\">Start</button> ";
        out_list += "<button class=\"btn btn-mini\" onclick=\"stop_test(" + test.id + ");\">Stop</button> ";
        out_list += "<button class=\"btn btn-mini\" onclick=\"reset_test(" + test.id + ");\">Reset</button> ";	
    }    	
    out_list += "</td>\n";
    return out_list;  
};

function select_all_tests() {
	$(".chk-test").prop("checked", true);
};

function unselect_all_tests() {
	$(".chk-test").prop("checked", false);
};

function select_category() {
	if ($("#chktest-ready").prop("checked")) {
		$("div.status-ready").each(function() {
			var el_id = $(this).prop("id");
			var id_parts = el_id.split("-");
			var id = id_parts[2];
			$("#chktest-" + id).prop("checked", true);
		});
	}
	if ($("#chktest-running").prop("checked")) {
		$("div.status-running").each(function() {
			var el_id = $(this).prop("id");
			var id_parts = el_id.split("-");
			var id = id_parts[2];
			$("#chktest-" + id).prop("checked", true);
		});
	}
	if ($("#chktest-failed").prop("checked")) {
		$("div.status-failed").each(function() {
			var el_id = $(this).prop("id");
			var id_parts = el_id.split("-");
			var id = id_parts[2];
			$("#chktest-" + id).prop("checked", true);
		});
	}
};

function get_time_string(status, starttime, endtime) {
  var duration = 0;
  var out_string = "";
  if (status !== "QUEUED" && status !== "NOT_DONE") {  
	  if (status === "DONE" || status === "FAILED") {
		  duration = endtime - starttime;
	  }
	  else if (status === "RUNNING" || status === "PREREQUISITES") {
		  var now = new Date();
		  var now_sec = now.getTime() / 1000;
		  duration = now_sec - starttime;
	  }
	  out_string = elapsed_time(duration);
  }  
  return out_string;
};

function get_status_div(status, prerequisites, canrun, id) {
  if (canrun === "false") {
	  return "<div id=\"status-icon-" + id + "\" class=\"status-icon status-impossible\"><span class=\"text-only\">Impossible</span></div>";
  }
  if (status === "DONE")
    return "<div id=\"status-icon-" + id + "\" class=\"status-icon status-done\"><span class=\"text-only\">Done</span></div>";
  else if (status === "FAILED")
    return "<div id=\"status-icon-" + id + "\" class=\"status-icon status-failed\"><span class=\"text-only\">Failed</span></div>";
  else if (status === "RUNNING")
    return "<div id=\"status-icon-" + id + "\" class=\"status-icon status-running\"><span class=\"text-only\">Running</span></div>";
  else if (status === "PREREQUISITES")
	    return "<div id=\"status-icon-" + id + "\" class=\"status-icon status-prerequisites\"><span class=\"text-only\">Prerequisites</span></div>";
  else if (status === "QUEUED")
    return "<div id=\"status-icon-" + id + "\" class=\"status-icon status-queued\"><span class=\"text-only\">Queued</span></div>";
  else if (status === "NOT_DONE")
  {
    if (prerequisites === "true")
      return "<div id=\"status-icon-" + id + "\" class=\"status-icon status-ready\"><span class=\"text-only\">Ready</span></div>";
    else
      return "<div id=\"status-icon-" + id + "\" class=\"status-icon status-not-ready\"><span class=\"text-only\">Not ready</span></div>";
  }
};

/**
 * Starts the execution of one or more tests (or rather, puts them into
 * the waiting queue)
 * @param test_id A comma-separated list of test IDs
 */
function start_test(test_id) {
  $("#status-icon-" + test_id).removeClass("status-ready").addClass("status-queued").html("<span class=\"text-only\">Queued</span>");
  $.ajax({
    url         : "/run?id=" + test_id,
    contentType : "application/json",
    success     : function(result) {
      setTimeout(function() {refresh_test_list(true);}, 1000);
      }
  });
};

/**
 * Resets one or more tests
 * @param test_id A comma-separated list of test IDs
 */
function reset_test(test_id) {
  //$("#status-icon-" + test_id).removeClass("status-ready").addClass("status-queued").html("<span class=\"text-only\">Queued</span>");
  $.ajax({
    url         : "/reset?id=" + test_id,
    contentType : "application/json",
    success     : function(result) {
      setTimeout(function() {refresh_test_list(true);}, 1000);
      }
  });
};

/**
 * Stops the execution of one or more tests
 * @param test_id A comma-separated list of test IDs
 */
function stop_test(test_id) {
  $("#status-icon-" + test_id).removeClass("status-running").addClass("status-failed").html("<span class=\"text-only\">Failed</span>");
  $.ajax({
    url         : "/stop?id=" + test_id,
    contentType : "application/json",
    success     : function(result) {
      setTimeout(function() {refresh_test_list(true);}, 1000);
      }
  });
};

function start_selected_tests() {
  var id_list = get_selected_tests();
  // Sends these IDs in the run request
  $.ajax({
    url         : "/run?id=" + id_list,
    contentType : "application/json",
    success     : function(result) {
      setTimeout(function() {refresh_test_list(true);}, 1000);
      }
  });
};

function stop_selected_tests() {
	var id_list = get_selected_tests();
	// Sends these IDs in the run request
	$.ajax({
	    url         : "/stop?id=" + id_list,
	    contentType : "application/json",
	    success     : function(result) {
	      setTimeout(function() {refresh_test_list(true);}, 1000);
	      }
	  });
	};
	
function reset_selected_tests() {
  var id_list = get_selected_tests();
  // Sends these IDs in the run request
  $.ajax({
    url         : "/reset?id=" + id_list,
    contentType : "application/json",
    success     : function(result) {
      setTimeout(function() {refresh_test_list(true);}, 1000);
      }
  });
};

function get_selected_tests() {
	  // Get list of test IDs that are selected
	  var id_list = "";
	  $(".chk-test:checked").each(function() {
	    var eid = $(this).prop("id");
	    var parts = eid.split("-");
	    var id = parts[1];
	    id_list += id + ",";
	  });
	  return id_list;
};


function periodical_refresh() {
  refresh_test_list(true);
  // Refresh the test list
  setTimeout(periodical_refresh, REFRESH_INTERVAL);
};

function show_benchmark_info() {
  $.ajax({
    url         : "/info",
    contentType : "application/json",
    success     : function(result) {
      var contents = "";
      contents += "<tr><th>OS name</th><td>" + result.osname + "</td></tr>\n";
      contents += "<tr><th>Version</th><td>" + result.osversion + "</td></tr>\n";
      contents += "<tr><th>Architecture</th><td>" + result.osarch + "</td></tr>\n";
      contents += "<tr><th>CPUs</th><td>" + result.numcpu + "</td></tr>\n";
      contents += "<tr><th>Threads allocated</th><td>" + result.threads + "</td></tr>\n";
      $("#benchmark-info").html(contents);
      }
  });
};

function save_benchmark() {
  $.ajax({
    url         : "/save",
    contentType : "application/json",
    /*success     : function(result) {
      var contents = "";
      contents += "<tr><th>OS name</th><td>" + result.osname + "</td></tr>\n";
      contents += "<tr><th>Version</th><td>" + result.osversion + "</td></tr>\n";
      contents += "<tr><th>Architecture</th><td>" + result.osarch + "</td></tr>\n";
      contents += "<tr><th>CPUs</th><td>" + result.numcpu + "</td></tr>\n";
      contents += "<tr><th>Threads allocated</th><td>" + result.threads + "</td></tr>\n";
      $("#benchmark-info").html(contents);
      }
    */
  });
};


/* Found from: http://stackoverflow.com/a/13323160 */
function elapsed_time(delta) // delta is the interval in *seconds*
{
    var ps, pm, ph, pd, min, hou, sec, days;
    if(delta<=59)
    {
        ps = (delta>1) ? "s": "";
        return Math.round(delta) + " second" + ps;
    }
    if(delta>=60 && delta<=3599)
    {
        min = Math.floor(delta/60);
        sec = delta-(min*60);
        pm = (min>1) ? "s": "";
        ps = (sec>1) ? "s": "";
        return min+" minute"+pm; //+" "+sec+" second"+ps;
    }
    if(delta>=3600 && delta<=86399)
    {
        hou = Math.floor(delta/3600);
        min = Math.floor((delta-(hou*3600))/60);
        ph = (hou>1) ? "s": "";
        pm = (min>1) ? "s": "";
        return hou+" hour"+ph; //+" "+min+" minute"+pm;
    } 
    if(delta>=86400)
    {
        days = Math.floor(delta/86400);
        hou =  Math.floor((delta-(days*86400))/60/60);
        pd = (days>1) ? "s": "";
        ph = (hou>1) ? "s": "";
        return days+" day"+pd; //+" "+hou+" hour"+ph;
    }
};

function create_plots(plot_list) {
  var out = "";
  for (var i = 0; i < plot_list.length; i++) {
	  var plot_id = plot_list[i];
	  out += "<div class=\"plot\">\n";
	  out += "<img id=\"plot-" + plot_id + "\" width=\"400\" />\n";
	  out += "<div>\n";
	  out += "<button class=\"btn btn-small\" title=\"Refresh plot\" onclick=\"refresh_plot(" + plot_id + "\"><span class=\"glyphicon glyphicon-refresh\"></span><span class=\"text-only\">Refresh plot</span></button> ";
	  out += "<a href=\"/plot?id=" + plot_id + "&amp;terminal=pdf&amp;download=true\"><button class=\"btn btn-small\" title=\"Download plot\"><span class=\"glyphicon glyphicon-download\"></span> PDF<span class=\"text-only\">Download plot</span></button> ";
	  out += "<a href=\"/plot?id=" + plot_id + "&amp;terminal=pdf&amp;download=true&amp;raw=true\"><button class=\"btn btn-small\" title=\"Download plot instructions\"><span class=\"glyphicon glyphicon-download\"></span> GP<span class=\"text-only\">Download plot instructions</span></button>";
	  out += "</div></div>\n";
  }
  $("#plots").html(out);
};

function refresh_plot(plot_id)
{
    // Refresh plot
	var img_src = "/plot?id=" + plot_id + "&amp;ts=" + new Date().getTime();
	(new Image()).src = img_src;
	for (var i = 0; i < 100000; i++) {
		// Wait a bit (unelegant)
	}
	// Now set the visible image with the same src; it should
	// be preloaded and not blink
    $("#plot-" + plot_id).attr("src", img_src);
};

function toggle_section(divname) {
	$(".div-section").hide();
	$("#section-" + divname).show();
};

$(document).ready(function() {
  $("#refresh-status").click(function() {refresh_test_list(true);});
  $("#check-all").click(select_all_tests);
  $("#uncheck-all").click(unselect_all_tests);
  $("#check-category").click(select_category);
  $("#start-all").click(start_selected_tests);
  $("#stop-all").click(stop_selected_tests);
  $("#reset-all").click(reset_selected_tests);
  $("#save-benchmark").click(save_benchmark);
  $("#refresh-plot").click(refresh_plot);
  show_benchmark_info();
  refresh_test_list(false);
  periodical_refresh();
});

