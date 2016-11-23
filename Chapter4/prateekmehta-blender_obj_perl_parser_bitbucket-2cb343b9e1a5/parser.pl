#!C:/wamp/bin/perl/bin/perl.exe
#Author: Prateek Mehta
#Purpose: blender extractor
#Releases:

	#Version: 0
		# > obj mesh and mtl Kd parsing
		# > 17 |July| - 21 |July| [2012]

	#Version: 1
		# > vertex-normal threshold method 1
		# > 23 |July| - 25 |July| [2012]

	#Version: 2
		# > vertex-normal threshold method 2
		# > 28 |July| - 30 |July| [2012]

	#Version: 3
		# > precision utility for normals
		# > 02 |Aug | - 05 |Aug | [2012]

use Math::BigFloat;
###########################################################################################################
####################################################
$file_name				= "";
$obj_file_name			= "";
$obj_file_parent_path	= "";
$obj_file_full_path		= "";
$mtl_file_full_path		= "";
$txt_file_full_path		= "";
@file_name_parts		= ();
@obj_file_lines			= ();
@mtl_file_lines			= ();
####################################################
print "Enter the name of obj file:\n";
$file_name = <STDIN>;
chomp( $file_name );
@file_name_parts = split( /\./,$file_name );
$obj_file_name = shift @file_name_parts;
$obj_file_parent_path = "C:/Blender/SENSOR/";

$obj_file_full_path = $obj_file_parent_path.$obj_file_name.".obj";
$mtl_file_full_path = $obj_file_parent_path.$obj_file_name.".mtl";
$txt_file_full_path = $obj_file_parent_path.$obj_file_name.".txt";

open( hanr,$obj_file_full_path );
@obj_file_lines = <hanr>;
close( hanr );
open( hanr,$mtl_file_full_path );
@mtl_file_lines = <hanr>;
close( hanr );
open( hanw,"+>".$txt_file_full_path );
#foreach( @obj_file_lines ){print $_,"\n";}
###########################################################################################################
####################################################
@o_material_names		= ();
$o_material_colors		= {};
####################################################
foreach( @mtl_file_lines ) {
  if( $_ =~ /newmtl\s([a-zA-Z]+)/ ) {
    push( @o_material_names,$1 );
  }
  if($_ =~ /Kd\s([0-9]+\.[0-9]+)\s([0-9]+\.[0-9]+)\s([0-9]+\.[0-9]+)/) {
    $r = $1."f,";
	$g = $2."f,";
	$b = $3."f";
	$o_material_colors->{@o_material_names[scalar(@o_material_names)-1]} = $r.$g.$b;
  }
}
#foreach( keys %{$o_material} ){print $_,"->",@{$o_material->{$_}},"\n";}
###########################################################################################################
####################################################
@obj_raw_normals		= ();
$o_raw_meshes			= {};
$o_occurence_order		= {};
$o						= "";
####################################################
foreach( @obj_file_lines ) {
  if( $_ =~ /vn\s([-]?[0-9]+\.[0-9]+.*)/ ) {
    push( @obj_raw_normals,$1 );
  }
}

foreach( @obj_file_lines ) {
  if( $_ =~ /o\s([a-zA-Z]+)/ ) {
    $position = scalar(keys %{$o_occurence_order});
	$o_occurence_order->{$position}	= $1;
	$o_raw_meshes->{$1}				= [];
	$o								= $1;
  }
  if( $_ =~ /f\s([0-9]+\/\/[0-9]+\s[0-9]+\/\/[0-9]+\s[0-9]+\/\/[0-9]+)/ ) {
    push( @{$o_raw_meshes->{$o}},$1 );
  }
}
#foreach( keys %{$o_raw_meshes} ){print $_ ,"->",@{$o_raw_meshes->{$_}},"\n\n";}
###########################################################################################################
####################################################
$mesh_parted_o				= {};
$key						= "";
$value						= "";
@o_smallest_vertex_index	= ();
####################################################
foreach( sort keys %{$o_occurence_order} ) {
  $key		= $_;
  $value	= $o_occurence_order->{$key};
  $o_smallest_vertex_index				= 15041992;
  $current_mesh_begin_index				= 0;
  $mesh_parted_o->{$value}				= {};
  $mesh_parted_o->{$value}->{mesh}		= [];
  $mesh_parted_o->{$value}->{normal}	= [];
  foreach( @{$o_raw_meshes->{$value}} ) {
	@parts	= split( /\s/,$_ );
	@f		= split( /\/\//,$parts[0] );
	@s		= split( /\/\//,$parts[1] );
	@t		= split( /\/\//,$parts[2] );
	$f		= shift( @f );
	$s		= shift( @s );
	$t		= shift( @t );
	if( $key > 0 ) {
	  $current_mesh_begin_index = $f<$s?($f<$t?$f:$t):($s<$t?$s:$t);
	  if( $o_smallest_vertex_index > $current_mesh_begin_index ) {
	    $o_smallest_vertex_index = $current_mesh_begin_index;
	  }
	}
    --$f;
    --$s;
    --$t;
    push( @{$mesh_parted_o->{$value}->{mesh}},$f.','.$s.','.$t );
    push( @{$mesh_parted_o->{$value}->{normal}},($f[0] - 1) );
  }
  push( @o_smallest_vertex_index,($o_smallest_vertex_index - 1) );
}
$o_smallest_vertex_index[0] = 0;

foreach( sort keys %{$o_occurence_order} ) {
  $key		= $_;
  $value	= $o_occurence_order->{$key};
  if( $key > 0 ) {
    foreach( @{$mesh_parted_o->{$value}->{mesh}} ) {
      $current_mesh_vertex_part				= $_;
	  @current_mesh_vertex_part_vertices	= split( /,/,$current_mesh_vertex_part );
	  $f	= $current_mesh_vertex_part_vertices[0] - $o_smallest_vertex_index[$key];
	  $s	= $current_mesh_vertex_part_vertices[1] - $o_smallest_vertex_index[$key];
	  $t	= $current_mesh_vertex_part_vertices[2] - $o_smallest_vertex_index[$key];
	  $_	= [$f,$s,$t];
    }
  } else {
    foreach( @{$mesh_parted_o->{$value}->{mesh}} ) {
      $current_mesh_vertex_part				= $_;
	  @current_mesh_vertex_part_vertices	= split( /,/,$current_mesh_vertex_part );
	  $f	= $current_mesh_vertex_part_vertices[0];
	  $s	= $current_mesh_vertex_part_vertices[1];
	  $t	= $current_mesh_vertex_part_vertices[2];
	  $_	= [$f,$s,$t];
    }
  }
}

foreach( sort keys %{$o_occurence_order} ) {
  $key		= $_;
  $value	= $o_occurence_order->{$key};
  foreach( @{$mesh_parted_o->{$value}->{normal}} ) {
    $current_mesh_normal_part = $obj_raw_normals[$_];
	@parts	= split( /\s/,$current_mesh_normal_part );
	$x		= $parts[0];
	$y		= $parts[1];
	$z		= $parts[2];
	$_		= [$x,$y,$z];
  }
}
#print $f,',',$s,',',$t,"\t",$current_mesh_begin_index,"\n",$o_smallest_vertex_index;
###########################################################################################################
####################################################
@o_vertices_count		= ();
$o_count				= -1;
$vertex_count			= 0;
$o_vertices				= {};
$o						= "";
####################################################
foreach( @obj_file_lines ) {
  if( $_ =~ /o\s([a-zA-Z]+)/ ) {
    $o_count++;
	$vertex_count = 0;
  }
  if( $_ =~ /v\s([-]?[0-9]+\.[0-9]+.*)/ ) {
    $vertex_count++;
	$o_vertices_count[$o_count] = $vertex_count;
  }
}

foreach( @obj_file_lines ) {
  if( $_ =~ /o\s([a-zA-Z]+)/ ) {
    $o					=	$1;
    $o_vertices->{$o}	=	[];
  }
  if( $_ =~ /v\s([-]?[0-9]+\.[0-9]+)\s([-]?[0-9]+\.[0-9]+)\s([-]?[0-9]+\.[0-9]+)/ ) {
    push( @{$o_vertices->{$o}},[$1,$2,$3] );
  }
}

$o_vertex_adjacent_mesh_indices = {};
foreach( sort keys %{$o_occurence_order} ) {
  $key		= $_;
  $value	= $o_occurence_order->{$key};
  $o_vertex_adjacent_mesh_indices->{$value} = {};
  foreach $index ( 0 .. ($o_vertices_count[$key] - 1) ) {
    $o_vertex_adjacent_mesh_indices->{$value}->{$index} = [];
    $mesh_index = -1;
    foreach $current_mesh ( @{$mesh_parted_o->{$value}->{mesh}} ) {
      $mesh_index++;
      foreach $current_vertex ( @{$current_mesh} ) {
        if( $index == $current_vertex ) {
          push( @{$o_vertex_adjacent_mesh_indices->{$value}->{$index}},$mesh_index );
          last;
        }
      }
    }
  }
}
#foreach( @{$o_vertices->{Nozzle}} ){foreach( @{$_} ){print $_,",";}print "\n";}
###########################################################################################################
####################################################
$o_vertex_adjacent_thresholded_surface_normals = {};
$cache_method			= 2;
$maximum_value_of_dot	= 3;
$threshold				= 0.33;
$cached_mesh_index		= 0;
@cached_normal			= ();
####################################################
if( $cache_method eq 1 ) {
  foreach( sort keys %{$o_occurence_order} ) {
    $key	= $_;
    $value	= $o_occurence_order->{$key};
    $o_vertex_adjacent_thresholded_surface_normals->{$value} = {};
    foreach $index ( 0 .. ($o_vertices_count[$key] - 1) ) {
      $cached_mesh_index = @{$o_vertex_adjacent_mesh_indices->{$value}->{$index}}[0];
      $o_vertex_adjacent_thresholded_surface_normals->{$value}->{$index} = [];
      push( @{$o_vertex_adjacent_thresholded_surface_normals->{$value}->{$index}},$cached_mesh_index );
      @cached_normal = @{@{$mesh_parted_o->{$value}->{normal}}[$cached_mesh_index]};
      $count = -1;
      foreach $normal_index ( @{$o_vertex_adjacent_mesh_indices->{$value}->{$index}} ) {
        $count++;
        if( $count > 0 ) {
          @normal	= @{@{$mesh_parted_o->{$value}->{normal}}[$normal_index]};
          $x		= $normal[0];
          $y		= $normal[1];
          $z		= $normal[2];
          $cached_normal_x	= $cached_normal[0];
          $cached_normal_y	= $cached_normal[1];
          $cached_normal_z	= $cached_normal[2];
          $product_x		= $x * $cached_normal_x;
          $product_y		= $y * $cached_normal_y;
          $product_z		= $z * $cached_normal_z;
          $sum				= $product_x + $product_y + $product_z;
          if( ($sum / $maximum_value_of_dot) >= $threshold ) {
            push( @{$o_vertex_adjacent_thresholded_surface_normals->{$value}->{$index}},$normal_index );
          }
        }
      }
    }
  }
}

if( $cache_method eq 2 ) {
  foreach( sort keys %{$o_occurence_order} ) {
    $key	= $_;
    $value	= $o_occurence_order->{$key};
    $o_vertex_adjacent_thresholded_surface_normals->{$value} = {};
    foreach $index ( 0 .. ($o_vertices_count[$key] - 1) ) {
      $o_vertex_adjacent_thresholded_surface_normals->{$value}->{$index} = [];
      $count = -1;
      foreach $normal_index ( @{$o_vertex_adjacent_mesh_indices->{$value}->{$index}} ) {
        $count++;
		if( $count eq 0 ) {
		  $cached_mesh_index = @{$o_vertex_adjacent_mesh_indices->{$value}->{$index}}[$count];
		  push( @{$o_vertex_adjacent_thresholded_surface_normals->{$value}->{$index}},$cached_mesh_index );
		  @cached_normal = @{@{$mesh_parted_o->{$value}->{normal}}[$cached_mesh_index]};
		}
        if( $count > 0 ) {
          @normal	= @{@{$mesh_parted_o->{$value}->{normal}}[$normal_index]};
          $x		= $normal[0];
          $y		= $normal[1];
          $z		= $normal[2];
          $cached_normal_x	= $cached_normal[0];
          $cached_normal_y	= $cached_normal[1];
          $cached_normal_z	= $cached_normal[2];
		  @cached_normal	= @{@{$mesh_parted_o->{$value}->{normal}}[$normal_index]};
          $product_x		= $x * $cached_normal_x;
          $product_y		= $y * $cached_normal_y;
          $product_z		= $z * $cached_normal_z;
          $sum				= $product_x + $product_y + $product_z;
          if( ($sum / $maximum_value_of_dot) >= $threshold ) {
            push( @{$o_vertex_adjacent_thresholded_surface_normals->{$value}->{$index}},$normal_index );
          }
        }
      }
    }
  }
}
#print $cached_normal_x,",",$cached_normal_y,",",$cached_normal_z,"  ",$normal_index,",nindex\n";
###########################################################################################################
print hanw "normal:\n\n";
foreach( sort keys %{$o_occurence_order} ) {
  $key		= $_;
  $value	= $o_occurence_order->{$key};
  foreach $index ( 0 .. ($o_vertices_count[$key] - 1) ) {
    $x		= 0;
	$y		= 0;
	$z		= 0;
	$count	= 0;
    foreach( @{$o_vertex_adjacent_thresholded_surface_normals->{$value}->{$index}} ) {
	  $count++;
	  @normal	= ();
	  @normal	= @{@{$mesh_parted_o->{$value}->{normal}}[$_]};
	  $x 		+= $normal[0];
	  $y		+= $normal[1];
	  $z		+= $normal[2];
	  if( $count eq scalar(@{$o_vertex_adjacent_thresholded_surface_normals->{$value}->{$index}}) ) {
	    $magnitude		= Math::BigFloat->new($x*$x + $y*$y + $z*$z);
	    $magnitude		= $magnitude->broot(2);
		$rx				= $x/$magnitude;
		$ry				= $y/$magnitude;
		$rz				= $z/$magnitude;
		@output			= ();
		@vertex_normal	= ();
		@output			= `Utility\\precision.pl $rx $ry $rz`;
		@vertex_normal			= split( /,/,$output[0] );
	    print hanw $vertex_normal[0],'f,',$vertex_normal[1],'f,',$vertex_normal[2],"f,\n";
	  }
	}
  }
  print hanw "\n\n";
}

print hanw "vertex:\n\n";
foreach( sort keys %{$o_occurence_order} ) {
  $key		= $_;
  $value	= $o_occurence_order->{$key};
  foreach( @{$o_vertices->{$value}} ) {
    foreach( @{$_} ) {
	  print hanw $_,"f,";
	}
	print hanw "\n";
  }
  print hanw "\n\n";
}

print hanw "index:\n\n";
foreach( sort keys %{$o_occurence_order} ) {
  $key		= $_;
  $value	= $o_occurence_order->{$key};
  print hanw "size:",(scalar(@{$mesh_parted_o->{$value}->{mesh}}) * 3),"\n";
  foreach( @{$mesh_parted_o->{$value}->{mesh}} ) {
    foreach( @{$_} ) {
	  print hanw $_,",";
	}
	print hanw "\n";
  }
  print hanw "\n\n";
}

print hanw "color:\n\n";
foreach( sort keys %{$o_occurence_order} ) {
  $key		= $_;
  $value	= $o_occurence_order->{$key};
  foreach $index ( 0 .. ($o_vertices_count[$key] - 1) ) {
    print hanw $o_material_colors->{$value},",1,\n";
  }
  print hanw "\n\n";
}
close( hanw );
###########################################################################################################