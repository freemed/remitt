Name:		remitt
Summary:	REMITT Electronic Medical Information Translation and Transmission
Version:	0.3.2
Release:	1
License:	GPL
Group:		System Environment/Daemons
URL:		http://www.remitt.org/
BuildArch:	noarch

Source0:	%{name}-%{version}.tar.gz

BuildRoot:	%{_tmppath}/%{name}-%{version}-%{release}-root

Requires:	perl, perl-XML-Parser, perl-libwww-perl, perl-XML-LibXML, perl-XML-LibXML-Common, perl-XML-Parser, perl-XML-Simple, perl-IO-Socket-SSL, perl-Net-SSLeay, perl-Config-IniFiles, curl, perl-PDF-API2, perl-WWW-Mechanize, perl-XML-LibXSLT, perl-CGI-Session
BuildPrereq:	make, perl

%description
REMITT is a second-generation medical bill generation system, which
is capable of accepting electronic medical record (EMR) information,
translating it into the appropriate format, and transmitting it to
the appropriate location.

REMITT is similar to FreeB (http://www.freeb.org/) in that it can
generate X12 837P and HCFA 1500 documents using an XML-RPC protocol,
but REMITT relies on sessions, a monolithic XML file, XSLT, and
other cutting edge technologies to provide vast speed and quality
improvements.

%prep

%setup

%build

%install
rm -fr %{buildroot}
mkdir -p %{buildroot}%{_datadir}/%{name}
for i in bin doc lib pdf remitt.conf spool test xsl; do \
	cp -Rvf $i %{buildroot}%{_datadir}/%{name}/; \
done

# Install seperate init script
mkdir -p %{buildroot}/etc/rc.d/init.d/
cp debian/remitt.initd.redhat %{buildroot}/etc/rc.d/init.d/%{name}
mkdir -p %{buildroot}/usr/sbin/
cp debian/%{name}-server %{buildroot}/usr/sbin/%{name}-server

%clean
rm -fr %{buildroot}

%post
/sbin/chkconfig --add %{name}

%preun
/etc/rc.d/init.d/%{name} stop
/sbin/chkconfig --remove %{name}

%postun

%files
%defattr(-,root,root)
/usr/sbin/%{name}-server
/etc/rc.d/init.d/%{name}
%{_datadir}/%{name}

%changelog

* Wed Jul 26 2006 Jeff Buchbinder <jeff@freemedsoftware.com> - 0.3.2-1
  - New upstream version.

* Sun Feb 19 2006 Jeff Buchbinder <jeff@freemedsoftware.com> - 0.3.1-1
  - New upstream version.

* Sat Aug 27 2005 Jeff Buchbinder <jeff@freemedsoftware.com> - 0.3-1
  - New upstream version.

* Wed Jan 06 2005 Jeff Buchbinder <jeff@freemedsoftware.com> - 0.2-1fc1
  - New upstream version.

* Wed Nov 03 2004 Jeff Buchbinder <jeff@freemedsoftware.com> - 0.1-1fc1
  - Initial Fedora Core 1 packaging (apologies to RH9 users)

