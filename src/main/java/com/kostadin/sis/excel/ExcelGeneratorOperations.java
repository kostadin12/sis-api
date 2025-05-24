package com.kostadin.sis.excel;

import com.kostadin.sis.excel.request.ExportToExcelCommand;
import com.kostadin.sis.excel.request.UserExportCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Tag(name = "Excel reports operations",description = """
        Endpoints that allow receiving different reports about a specific project and its members.
        """)
public interface ExcelGeneratorOperations {
    @Operation(
            summary = "Project members report.",
            description = """
                    Downloads an Excel file containing info about the project and the project members.
                    
                    Method is used in the Project Details view, when the user clicks the button above the members table, the file is downloaded.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "200", description = "Downloaded file successfully.", content = @Content(mediaType = "application/octet-stream"))
    void exportProjectMembers(HttpServletResponse response, @Parameter(name = "projectId", description = "Project id of exported project members", example = "1") long projectId) throws IOException;

    @Operation(
            summary = "Project members report (days off + days available)",
            description = """
                    Downloads an Excel file containing info about the project, project members, and the count of days where they're absent or available for a specific period of time.
                    Users could also be filtered by labels / employee numbers. All project members' available days could be reduced by selecting a period capacity cap. (0-100%)
                    
                    Method is used in the Project Calendar view - by clicking the 'Export' button, a modal would appear where the user has to fill in the required information. Accessible only for 'ADMIN' or 'MANAGER' users.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "200", description = "Downloaded file successfully.", content = @Content(mediaType = "application/octet-stream"))
    void exportProjectMembersAbsences(HttpServletResponse response, ExportToExcelCommand exportToExcelCommand) throws IOException;

    @Operation(
            summary = "Detailed user(s) report",
            description = """
                    Downloads an Excel file containing info about users and the count of days where they're absent or available for a specific period of time. Including each of the projects they participate in and their capacity.
                    There are 3 categories to choose and filter the users from - employee numbers, company names, or 'SYSTEM' scoped labels.
                    
                    Method is used for extracting a detailed report about users. They could be filtered by names / employee numbers, companies, or labels.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "200", description = "Downloaded file successfully.", content = @Content(mediaType = "application/octet-stream"))
    void exportUserReport(HttpServletResponse response, UserExportCommand userExportCommand) throws IOException;
}
